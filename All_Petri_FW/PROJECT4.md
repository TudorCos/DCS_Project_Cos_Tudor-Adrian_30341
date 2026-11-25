# DCS Project 4: four-phase traffic-light controller

Run the existing **FuzzyUWU Controller** IntelliJ configuration. It runs
`FuzzyUWU.TrafficLightController` with first lane port 1081. Start the lane
applications first so their network listeners are ready, then click **Start**
in the controller window. The controller sends to `P_TL1`...`P_TL4` on ports
1081...1084. No change to the lane, intersection, exit, roundabout, or alley
configurations is required.

The new controller follows the diagram's single-lane progression:

| Stage | Lane 1 | Lane 2 | Lane 3 | Lane 4 |
|---|---:|---:|---:|---:|
| Initialize / AllRed | -1 | -1 | -1 | -1 |
| Lane1Green | 0 | -1 | -1 | -1 |
| Lane1Yellow | +1 | -1 | -1 | -1 |
| Lane2Green | -1 | 0 | -1 | -1 |
| Lane2Yellow | -1 | +1 | -1 | -1 |
| Lane3Green | -1 | -1 | 0 | -1 |
| Lane3Yellow | -1 | -1 | +1 | -1 |
| Lane4Green | -1 | -1 | -1 | 0 |
| Lane4Yellow | -1 | -1 | -1 | +1 |
| AllRed (five ticks) | -1 | -1 | -1 | -1 |
| Repeat at Lane1Green | 0 | -1 | -1 | -1 |

The state places are `DataFuzzy`, moved through the identity FLRS. Signals are
constant fuzzy places: Red=-1 (NL), Green=0 (ZR), Yellow=+1 (PL). Each `TL_outN`
is a `DataTransfer`. The `d1`...`d4` places are `DataInteger` with initial value 5;
they live in the controller's regular place list so the Project 5 supervisor
can update them by sending integer tokens to controller port 1102.

The net tick is 1000 ms. The controller reads `dN` when it schedules lane N's
green-to-yellow transition. Updates received during an already scheduled green
phase take effect when that lane next turns green. Each yellow phase lasts
five ticks. After lane 4 turns red, all four lanes stay red for another five
ticks before lane 1 turns green.

To verify, watch the four lane windows' `P_TL1`...`P_TL4` values. Only one should
be 0 at a time. During the lane 1-to-2, 2-to-3, and 3-to-4 handoffs, the
preceding lane becomes -1 when the next becomes 0. Yellow
(+1) blocks movement just like red in the lane model. The controller sends all
four reds once at startup, then only the colors that change at each stage.

`FuzzyUWU.Project4ControllerCheck` runs without windows. It fires the real
controller transitions, captures each outgoing fuzzy signal, checks the order,
ports, target places, repeat cycle, and how the scheduler reads duration updates. It does not
measure wall-clock timing or exercise the GUI network listeners.

## Run with the Project 5 supervisor

For separate windows, start **FuzzyUWU Supervisor** and click **Pause** so
its listener can collect the initial flows without sending durations yet.
Start the intersection, exits, and four lane applications, then start the
controller. Click **Start** in the supervisor window again to resume it and
send the durations. **FuzzyUWU System** starts all models, including the
supervisor, together.

Use the fuzzy input sender (`GUIs.InputFuzzy`) to submit a sample to each
entrance lane. Send to `P_i1` on 1081, `P_st` on 1082, `P_i3` on 1083, and
`P_i4` on 1084. Use `0.0` for no flow and `1.0` for a jam. Each sample is
forwarded to the corresponding `f1` through `f4` on supervisor port 1101.
For a supervisor-only input test, send to those four places directly.

`T0` consumes one sample per lane and produces the four durations. It waits
for another sample from every lane before calculating again. `T1` through
`T4` send the durations through `OP1` through `OP4` to the matching controller
places on port 1102. With only lane 1 jammed, the result is `8, 4, 4, 4`.
