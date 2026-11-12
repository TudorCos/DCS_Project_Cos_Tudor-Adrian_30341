# DCS Project 1: fuzzy rule exercises

## What the sheet asks

The introductory diagrams explain the framework. The exercises at the bottom ask to:

1. Change the 1-input/1-output and 2-input/1-output FLRS examples so the output equals the input.
2. Copy `reader` from `DCS_FuzzyLab.OETPN_C.PIController` into the 2-input/1-output example, set P2's input arc weight to 1, and observe the result.

With two inputs, “output = input” does not identify which input. Here we use **P2**, consistent with the specified `reader` table. If P1 is intended instead, use constant rows: all NL in row NL, all NM in row NM, all ZR in row ZR, all PM in row PM, all PL in row PL.

## 1. One input, one output

Changed `All_Petri_FW/src/TestFuzzy/TestFLRS/FLRS1X1.java` to use:

| Input | NL | NM | ZR | PM | PL |
|---|---|---|---|---|---|
| Output | NL | NM | ZR | PM | PL |

These labels have centers -1, -0.5, 0, 0.5, 1. Each input label maps to itself. The input arc weight is 1.

Example: x = 0.1 has membership vector <0, 0, 0.8, 0.2, 0>. The rules activate ZR -> ZR and PM -> PM. The output is 0.8*0 + 0.2*0.5 = **0.1**.

The example now starts with P1=0.1 instead of requiring the unavailable `D:\PetriInputData\test3.txt` file. P2 starts empty; firing T1 consumes P1 and creates P2=0.1.

## 2. Two inputs, one output: reader

Changed `All_Petri_FW/src/TestFuzzy/TestFLRS/FLRS2X1.java` to use the same 25 entries as `PIController.reader`:

| P1 \\ P2 | NL | NM | ZR | PM | PL |
|---|---|---|---|---|---|
| NL | NL | NM | ZR | PM | PL |
| NM | NL | NM | ZR | PM | PL |
| ZR | NL | NM | ZR | PM | PL |
| PM | NL | NM | ZR | PM | PL |
| PL | NL | NM | ZR | PM | PL |

Every row is identical, so P1's numeric value does not affect the output. Both input tokens must still be present to enable T1. The framework iterates P1 in the outer loop and P2 in the inner loop when constructing this table.

For normalized inputs, the framework computes the sum of each rule's output center multiplied by both input membership degrees. Because the memberships of P1 sum to 1, this reduces to the identity mapping of the weighted P2 value.

| P1 | P2 | P1 weight | P2 weight | Output with reader |
|---|---|---|---|---|
| 0.1 | 0.2 | 1 | -1 | -0.2 |
| 0.1 | 0.2 | 1 | +1 | +0.2 |
| -0.75 | 0.2 | 1 | +1 | +0.2 |
| 0.1 | 0 | 1 | +1 | 0 |

The first two rows isolate the effect of the weight **using the reader table in both cases**. The original unrelated 25-entry table need not produce -0.2.

Weights multiply crisp values **before fuzzification**. Changing P2's weight from -1 to +1 removes its sign inversion. With the initial markings P1=0.1 and P2=0.2, firing T1 consumes both tokens and produces P3 approximately 0.2. Java may print 0.20000002 due to float arithmetic.

The identity result has the precision of this framework: membership degrees are rounded to two decimal places. Values outside [-1,1] saturate at the endpoint membership labels. Zero is a valid fuzzy token; FF denotes no output token.

## Run in IntelliJ

1. Open `All_Petri_FW/pom.xml` as a Maven project and select JDK 11 or newer. Allow Maven to resolve JFreeChart.
2. Run `TestFuzzy.TestFLRS.FLRS1X1`. Click **Start** in the Petri net window and observe P2=0.1.
3. Close/stop that example before running `TestFuzzy.TestFLRS.FLRS2X1`, since both use port 1081. Click **Start** and observe P3 approximately 0.2.
4. For the weight comparison, change `new PlaceNameWithWeight("P2", 1F)` to `-1F`, rerun, and observe approximately -0.2. Restore 1F for the final exercise.
5. Run `TestFuzzy.TestFLRS.Project1Check` for verification without windows. It checks 1x1 identity and reader behavior with both weight signs at endpoints, label centers, zero, and intermediate values.

## Verification performed

All Java sources compiled with JDK 11 and the locally cached JFreeChart 1.5.3 dependency. `Project1Check` passed **210 checks**, using the framework's actual `Activation` implementation. GUI button operation and networking were not exercised by this check.
