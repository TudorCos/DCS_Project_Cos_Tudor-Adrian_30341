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
| Repeat at Lane1Green | 0 | -1 | -1 | -1 |

The state places are `DataFuzzy`, moved through the identity FLRS. Signals are
constant fuzzy places: Red=-1 (NL), Green=0 (ZR), Yellow=+1 (PL). Each `TL_outN`
is a `DataTransfer`. The `d1`...`d4` places are `DataInteger` with initial value 5;
they live in `ConstantPlaceList` because the framework's `DynamicDelay`
activation reads an unbooked value from that list. A yellow transition also
requires its corresponding duration to be present.

Each phase transition starts with `Delay=5` and the net tick is 1000 ms. Thus
color changes are nominally five seconds apart, subject to Java scheduling and
network activity. `DynamicDelay` is attached to each green-to-yellow transition
as the sheet requests. **Framework detail:** it assigns `transition.Delay` only
when that transition fires, so editing `dN` affects the *next cycle's* duration
for that green-to-yellow transition. It cannot adjust an already booked firing.
The `dN` values are not exposed through a network input in this version.

To verify, watch the four lane windows' `P_TL1`...`P_TL4` values. Only one should
be 0 at a time. The preceding lane becomes -1 when the next becomes 0. Yellow
(+1) blocks movement just like red in the lane model. The controller sends all
four reds once at startup, then only the colors that change at each stage.

`FuzzyUWU.Project4ControllerCheck` runs without windows. It fires the real
controller transitions, captures each outgoing fuzzy signal, checks the order,
ports, target places, repeat cycle, and `DynamicDelay` behavior. It does not
measure wall-clock timing or exercise the GUI network listeners.
