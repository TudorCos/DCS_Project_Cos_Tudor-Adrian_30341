# Current version: Project 3

Read [PROJECT3.md](PROJECT3.md) first for the current lane 2, roundabout, and alley setup.
The Project 2 walkthrough below is retained as background. Its instruction to send
`P_i` to EntranceLane2 is superseded: lane 2 now requires `P_R` and `P_st`.
ExitLane2 now forwards its result to the roundabout instead of retaining a terminal
fuzzy `P_Exit` value. The other entrance/exit lanes and intersection are unchanged.

---

# FuzzyUWS - running with two entrance lanes

## What "two lanes" means in the current model

This walkthrough uses **EntranceLane1 and EntranceLane2** for traffic.
EntranceLane3 and EntranceLane4 do not need to run. You supply zero-flow tokens
for them directly to the intersection.

The intersection still has **four outputs** and always splits flow into all four.
Run all four exit lanes to receive those outputs. Starting only ExitLane1 and
ExitLane2 does not make this a two-exit model: flow is still sent to exits 3 and 4.
A genuine two-entrance/two-exit model requires a code change to the intersection,
its split weights, and the controller; there is no two-lane configuration flag yet.

## 1. Open and run the classes

Open `All_Petri_FW/pom.xml` as a Maven project in IntelliJ with JDK 11 or newer.
Open each Java file and click the green arrow beside `main`.
The dedicated lane files need no lane-number arguments.

Start these classes in order and click **Start** in each Petri net window:

| Order | Class | Program arguments | Port |
|---|---|---|---|
| 1 | `FuzzyUWS.ExitLane1` | none | 1111 |
| 2 | `FuzzyUWS.ExitLane2` | none | 1112 |
| 3 | `FuzzyUWS.ExitLane3` | none | 1113 |
| 4 | `FuzzyUWS.ExitLane4` | none | 1114 |
| 5 | `FuzzyUWS.Intersection` | none | 1100 |
| 6 | `FuzzyUWS.EntranceLane1` | none | 1101 |
| 7 | `FuzzyUWS.EntranceLane2` | none | 1102 |
| 8 | `GUIs.InputFuzzy` | none | sends to the port you enter |

Leave **EntranceLane3, EntranceLane4, and Controller stopped** for this walkthrough.
The current controller sends light signals to all four entrance processes and
cycles through all four lanes; it is not configured for only two running entrances.
Use manual signals as described below.

Start receivers before sending data. The framework has no application-level
acknowledgement or retry for these transfers.

## 2. Understand the input window

`GUIs.InputFuzzy` has three text fields:

- Top field: destination **place name**, such as `P_i`.
- Field just below it: numeric **value**, such as `0.1`.
- Bottom field above Send: destination **port**, such as `1101`.

Replace the existing text in each field, then click **Send** for each row in the
tables below. Type values directly; the number buttons append characters.
Use exact place names, including capitalization. The host is localhost.

## 3. Supply zero flow for the unused entrances

Send these two messages to the running intersection:

| Place name | Value | Port | Meaning |
|---|---|---|---|
| `P_i3` | `0` | `1100` | No flow from entrance 3 |
| `P_i4` | `0` | `1100` | No flow from entrance 4 |

**Zero and missing are different.** Zero is a valid fuzzy token. A missing token
means the transition waits. Without these messages, the intersection cannot
complete a batch even when lanes 1 and 2 have supplied traffic.

The pair of zero tokens may already move into `Pair34=0`; that is expected.

## 4. Put traffic into the two active entrance lanes

Send:

| Place name | Value | Port |
|---|---|---|
| `P_i` | `0.1` | `1101` |
| `P_i` | `0.1` | `1102` |

Each flow moves through `P_i -> P_l -> Lane`. Both lanes initially have a red
light (`P_TL=-1`), so the traffic waits at `Lane`.

## 5. Give each entrance a green signal

Send:

| Place name | Value | Port |
|---|---|---|
| `P_TL` | `0` | `1101` |
| `P_TL` | `0` | `1102` |

Green is **0**, red is **-1**, and yellow is **+1**. Only green permits passage.
For this aggregate-flow demonstration both lanes may be green together; it is
not a simulation of collision-safe vehicle trajectories. To demonstrate separate
phases, turn lane 1 green, wait for its transfer, turn it red, then turn lane 2 green.

`t_out` checks the light against the constant fuzzy place `Green=0`, moves the
flow to `P_out`, and leaves the light signal available. `t_network` sends the flow
to intersection `P_i1` or `P_i2`. Places may empty quickly after transitions fire;
watch the console logs as well as the current marking.

## 6. Expected result

The intersection computes:

```text
Pair12 = 0.1 + 0.1 = 0.2
Pair34 = 0   + 0   = 0
P_I    = 0.2 + 0   = 0.2
```

Then `t_split` uses `Prod_Fuzzy` and constant fuzzy weights:

| Exit | Weight | Split value | Final P_Exit (approximately) |
|---|---|---|---|
| 1 | 0.25 | 0.05 | 0.05 |
| 2 | 0.50 | 0.10 | 0.10 |
| 3 | 0.12 | 0.024 | 0.025 |
| 4 | 0.12 | 0.024 | 0.025 |

The final exit identity FLRS uses membership degrees rounded to two decimal
places, so some values change slightly. Small Java float printing differences
are also normal. Weights total **0.99**, following the sheet literally despite
its "Total=1" annotation.

All inputs are pooled before splitting: entrance 1 does not feed only exit 1.
The split distributes combined flow to every exit.

## 7. Send another batch

Wait until the previous batch has reached all four `P_Exit` places. Repeat the
zero-token messages for intersection `P_i3` and `P_i4`, then send the next traffic
values to entrance `P_i` on ports 1101 and 1102. The zero tokens were consumed
by the previous batch; they are not permanent defaults.

Green signals persist until replaced, so you only need to send them again if
you changed them to red or yellow. To test gating, send red first, then traffic,
observe it waiting, and finally send green.

Each place holds one token, not a queue. Incoming network data can overwrite an
occupied place. `P_Exit` shows the latest result, not a running total. Use normalized
traffic values in [0,1]; the fuzzy addition tables saturate near the domain limits.

## Alternative: test just the intersection

You can omit both entrance processes and manual light messages. Run all four
exits and the intersection, then use InputFuzzy to send `P_i1=0.1`, `P_i2=0.1`,
`P_i3=0`, and `P_i4=0`, all to port **1100**. This tests combination and splitting
but bypasses entrance movements and traffic-light gating.

## Run the original four-lane demo

Run all four exit lanes and the intersection first. Run EntranceLane1..4 with
**Program arguments: `demo`** (set in Run > Edit Configurations). Each starts
with one flow token of 0.1. Click Start in every receiver window, then run and
start `FuzzyUWS.Controller` on port 1120 last.

Its phase sequence is G1,Y1,G2,Y2,G3,Y3,G4,Y4, with other lanes red. Each phase
nominally lasts five one-second framework ticks. Actual wall-clock duration
also depends on scheduling and network work. This phase order is a design choice.
The expected combined flow is 0.4, split into 0.1, 0.2, 0.048, 0.048, and finally
approximately 0.1, 0.2, 0.05, 0.05 after the exit FLRS.

## Files and checks

EntranceLane1..4 and ExitLane1..4 are separate runnable entry points. Shared
construction lives in EntranceLane, ExitLane, and ModelSupport. Intersection
contains the combination/splitting model; Controller contains the light cycle.
Supervisor reporting marked "not yet" in the sheet is omitted.

Run `FuzzyUWS.ModelCheck` for the existing 42 checks of gating, retained signals,
combination, splitting, exit quantization, zero-flow batches, and controller delay
configuration. Those checks use actual framework transitions without network
sends or GUI operation. Live delivery and measured timing require a GUI run.
