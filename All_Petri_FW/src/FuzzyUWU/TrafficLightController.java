package FuzzyUWU;

import Components.Activation;
import Components.Condition;
import Components.GuardMapping;
import Components.PetriNet;
import Components.PetriTransition;
import DataObjects.DataInteger;
import Enumerations.LogicConnector;
import Enumerations.TransitionCondition;
import Enumerations.TransitionOperation;

public class TrafficLightController {
    public static final int FIRST_LANE_PORT = 1081;
    public static final int SIGNAL_SECONDS = 5;

    public static PetriNet build(int firstLanePort) {
        PetriNet pn = new PetriNet();
        pn.PetriNetName = "Fuzzy traffic-light controller";
        pn.SetName("TrafficController");
        pn.NetworkPort = 0;
        pn.Delay = 1000;
        pn.PlaceList.add(FuzzyModelSupport.fuzzyPlace("Initialize", 0F));
        pn.PlaceList.add(FuzzyModelSupport.fuzzyPlace("AllRed"));
        for (int lane = 1; lane <= 4; lane++) {
            pn.PlaceList.add(FuzzyModelSupport.fuzzyPlace("Lane" + lane + "Green"));
            pn.PlaceList.add(FuzzyModelSupport.fuzzyPlace("Lane" + lane + "Yellow"));
            DataInteger duration = new DataInteger();
            duration.SetName("d" + lane);
            duration.SetValue(SIGNAL_SECONDS);
            pn.ConstantPlaceList.add(duration);
            pn.PlaceList.add(FuzzyModelSupport.transfer("TL_out" + lane,
                    firstLanePort + lane - 1, "P_TL" + lane));
        }
        pn.ConstantPlaceList.add(FuzzyModelSupport.fuzzyPlace("Red", -1F));
        pn.ConstantPlaceList.add(FuzzyModelSupport.fuzzyPlace("Green", 0F));
        pn.ConstantPlaceList.add(FuzzyModelSupport.fuzzyPlace("Yellow", 1F));

        PetriTransition initialize = phase(pn, "t_initialize", "Initialize", "AllRed", 0);
        for (int lane = 1; lane <= 4; lane++) signal(initialize, lane, "Red");

        PetriTransition firstGreen = phase(pn, "t_green1", "AllRed", "Lane1Green", SIGNAL_SECONDS);
        signal(firstGreen, 1, "Green");
        for (int lane = 1; lane <= 4; lane++) {
            String green = "Lane" + lane + "Green";
            String yellow = "Lane" + lane + "Yellow";
            PetriTransition startYellow = phase(pn, "t_yellow" + lane, green, yellow, SIGNAL_SECONDS);
            startYellow.GuardMappingList.get(0).condition.SetNextCondition(LogicConnector.AND,
                    new Condition(startYellow, "d" + lane, TransitionCondition.NotNull));
            signal(startYellow, lane, "Yellow");
            // DynamicDelay reads dN when this transition fires. The framework applies
            // it to its next firing; its initial Delay is SIGNAL_SECONDS as well.
            startYellow.GuardMappingList.get(0).Activations.add(
                    new Activation(startYellow, "d" + lane, TransitionOperation.DynamicDelay, ""));
            int next = lane == 4 ? 1 : lane + 1;
            PetriTransition startGreen = phase(pn, "t_green" + next + "_after" + lane,
                    yellow, "Lane" + next + "Green", SIGNAL_SECONDS);
            signal(startGreen, lane, "Red");
            signal(startGreen, next, "Green");
        }
        return pn;
    }

    private static PetriTransition phase(PetriNet pn, String name, String from, String to, int delay) {
        PetriTransition transition = new PetriTransition(pn);
        transition.TransitionName = name;
        transition.InputPlaceName.add(from);
        GuardMapping guard = new GuardMapping();
        guard.condition = FuzzyModelSupport.allNotNull(transition, from);
        guard.Activations.add(new Activation(transition, FuzzyModelSupport.moveRules(),
                FuzzyModelSupport.fuzzyInputs(from), TransitionOperation.FLRS, FuzzyModelSupport.outputs(to)));
        transition.GuardMappingList.add(guard);
        transition.Delay = delay;
        pn.Transitions.add(transition);
        return transition;
    }

    private static void signal(PetriTransition transition, int lane, String color) {
        transition.GuardMappingList.get(0).Activations.add(new Activation(transition,
                color, TransitionOperation.SendOverNetwork, "TL_out" + lane));
    }

    public static void main(String[] args) {
        int firstLanePort = args.length > 0 ? Integer.parseInt(args[0]) : FIRST_LANE_PORT;
        FuzzyModelSupport.show(build(firstLanePort));
    }
}
