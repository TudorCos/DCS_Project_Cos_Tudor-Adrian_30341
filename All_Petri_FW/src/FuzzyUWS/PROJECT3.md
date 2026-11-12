# DCS Project 3 - lane 2, roundabout, and alley

## Diagram interpretation and implementation

Only entrance lane 2 and exit lane 2 change. Lanes 1, 3, and 4, the main
intersection, and the light controller retain their previous behavior.

```text
Alley.P_i -> Alley.P_out (DataTransfer) -> EntranceLane2.P_st
Roundabout.P_out3 (DataTransfer)        -> EntranceLane2.P_R

EntranceLane2: (P_R + P_st) -> P_l -> Lane -> [green] -> P_out
                  -> main Intersection.P_i2

main Intersection -> ExitLane2.P_o -> ExitFlow
                  -> ExitLane2.P_Exit (DataTransfer) -> Roundabout.P_i1

Roundabout: P_i1 -> L1, P_i2 -> L2, P_i3 -> L3
            L1 + L2 -> L1L2; L1L2 + L3 -> L1L2L3
            split into E1,E2,E3, each = L1L2L3 * 0.33
            E1 -> P_out1 (local fuzzy output)
            E2 -> P_out2 (local fuzzy output)
            E3 -> ReturnFlow -> P_out3 (DataTransfer) -> EntranceLane2.P_R
```

The '+' transitions use the existing saturated fuzzy-adder table. Identity FLRS
moves are used where the sheet says Move. The 0.33 share is a constant fuzzy
place. The three shares total 0.99; they are not replaced with 1/3.

The framework's FLRS implementation creates DataFuzzy outputs and cannot write
directly into a DataTransfer place. ExitFlow and ReturnFlow therefore stage the
identity-FLRS results, and a separate network transition sends them through the
specified DataTransfer endpoints. The alley sends its fuzzy input directly.
Supervisor reporting remains omitted, as marked 'not yet' on the sheet.

## Run configuration

Open the Java file and click Run beside main. Dedicated lane classes need no
lane-number argument. Click **Start** in each Petri net window.

| Class | Port | Inputs | Outputs |
|---|---|---|---|
| EntranceLane2 | 1102 | P_R, P_st, P_TL | main intersection P_i2 |
| ExitLane2 | 1112 | P_o | roundabout P_i1 |
| Roundabout | 1130 | P_i1, P_i2, P_i3 | P_out1, P_out2, feedback to lane 2 |
| Alley | 1131 | P_i | lane 2 P_st |

All class names have prefix `FuzzyUWS.`. For full-system operation, start all
entrance/exit models plus Intersection, Roundabout, and Alley before starting
the Controller last. Start receivers before injecting flow. Roundabout and
ExitLane2 form a feedback loop, so start both empty before supplying tokens.

For manual testing, leave Controller stopped and set P_TL yourself. Green=0,
red=-1, yellow=+1. EntranceLane2 starts red. Its optional `demo` argument seeds
P_R=0.1 and P_st=0, once; omit it for the exact tests below.

## Small test A: roundabout to lane 2, without the full system

Run and Start **Roundabout**, **EntranceLane2** (no arguments), and **Alley**.
Run `GUIs.InputFuzzy`. Its top field is place name, next field is value, bottom
field is destination port. Replace each field's contents and click Send for
each message below. Keep lane 2 red so no main-intersection receiver is needed.

| Place | Value | Port |
|---|---|---|
| P_i1 | 0.3 | 1130 |
| P_i2 | 0.1 | 1130 |
| P_i3 | 0.1 | 1130 |
| P_i | 0.1 | 1131 |

Roundabout combines 0.3+0.1+0.1 into **0.5** and splits into **0.165** each.
Watch Roundabout.P_out1 and P_out2: both should be approximately **0.165**.
Its third output is sent to lane 2's P_R. Alley supplies P_st=0.1, so lane 2
combines them into **0.265**, which waits at **Lane** while the light is red.
P_R, P_st, and intermediate places can empty quickly after their tokens move;
use console logs to see the individual transitions.

Do not turn lane 2 green unless Intersection is running to receive its output.
To send that flow through the full main intersection, start all four exit lanes
(including the modified ExitLane2), start Intersection, send intersection
P_i1=0, P_i3=0, P_i4=0 to port 1100, then send lane 2 P_TL=0 to port 1102.

## Small test B: exit lane 2 to roundabout

Use fresh processes (or empty input places). Run and Start Roundabout,
EntranceLane2 with no arguments, and ExitLane2. Leave lane 2 red.
Send these messages with InputFuzzy:

| Place | Value | Port |
|---|---|---|
| P_o | 0.3 | 1112 |
| P_i2 | 0.1 | 1130 |
| P_i3 | 0.1 | 1130 |

ExitLane2 transfers **0.3** to Roundabout.P_i1. Again the roundabout's local
outputs should become approximately **0.165**. Lane 2 P_R receives the third
share but waits because P_st is missing. Send **P_st=0 to port 1102** to supply
an explicit zero alley flow; lane 2 then moves approximately 0.165 to Lane.

## Continuing batches and avoiding a stalled loop

Each roundabout batch requires all three P_i tokens. P_i1 comes from ExitLane2;
P_i2 and P_i3 are external roundabout inputs to supply using InputFuzzy. Send
explicit zeros if those roads have no traffic. These values are consumed, so
repeat them for every batch.

Each lane 2 batch requires both a roundabout P_R token and an alley P_st token.
A zero alley token is valid but is also consumed. The alley is not an automatic
flow generator; send fresh values to Alley.P_i, or directly to lane 2 P_st.

The loop cannot generate its first token from empty places. Seed one location,
such as the roundabout's three inputs (test A) or ExitLane2.P_o (test B).
Do not inject competing seeds into an already occupied input. The main
intersection also still requires tokens from entrances 1, 3, and 4, or explicit
zeros directly into its corresponding input places for every batch.

All places hold one token, not queues. Wait for a batch to finish before sending
another. Local output places retain the most recent result. Float arithmetic and
membership rounding can slightly change values; additions saturate at the fuzzy
domain limits. Network sends have no application-level retry or acknowledgement.

## Checks performed

All sources compile with JDK 11. `FuzzyUWS.ModelCheck` still passes 42 checks,
adapted to the new lane 2 and exit staging place. `FuzzyUWS.Project3Check` checks
the two-input merge, missing-input waiting, red gating, exit-to-roundabout route,
three-way combination and 0.33 splitting, local outputs, feedback, fresh alley
token requirement, and preservation of the other lane models.

Checks execute actual framework transitions and verify endpoint configuration;
they simulate delivery between models without opening sockets. A further `FuzzyUWS.Project3NetworkCheck` now passes using real localhost sockets
and framework scheduler threads. It verifies roundabout -> lane 2, alley -> lane 2,
lane 2 -> intersection, intersection -> all four exits, and exit 2 -> roundabout.
For the test-A flow of 0.265, measured terminal outputs are 0.065, 0.03, 0.03
(exits 1, 3, 4); the returned flow reaches roundabout L1 as 0.13 after repeated
identity-FLRS rounding. These are framework float/quantization results, not exact
real-number arithmetic.

Run this network check only with the GUI models stopped: it needs ports 1100,
1102, 1111..1114, 1130, and 1131. It accelerates scheduler ticks to 50 ms and
exits its isolated Java process on success. GUI rendering, controller timing,
concurrent traffic stress, and unattended continuous generation are not covered.
The model still needs fresh external/zero tokens for every synchronized batch.
