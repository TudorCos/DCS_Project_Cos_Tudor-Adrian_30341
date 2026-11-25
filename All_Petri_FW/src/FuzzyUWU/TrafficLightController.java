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
    public static final int NETWORK_PORT = 1102;
    public static final int SIGNAL_SECONDS = 5;

    public static PetriNet build(int firstLanePort) {
        return build(firstLanePort, NETWORK_PORT);
    }

    public static PetriNet build(int firstLanePort, int controllerPort) {
        PetriNet pn = new PetriNet();
        pn.PetriNetName = "Fuzzy traffic-light controller";
        pn.SetName("TrafficController");
        pn.NetworkPort = controllerPort;
        pn.Delay = 1000;
        pn.PlaceList.add(FuzzyModelSupport.fuzzyPlace("Initialize", 0F));
        pn.PlaceList.add(FuzzyModelSupport.fuzzyPlace("AllRed"));
        for (int lane = 1; lane <= 4; lane++) {
            pn.PlaceList.add(FuzzyModelSupport.fuzzyPlace("Lane" + lane + "Green"));
            pn.PlaceList.add(FuzzyModelSupport.fuzzyPlace("Lane" + lane + "Yellow"));
            DataInteger duration = new DataInteger();
            duration.SetName("d" + lane);
            duration.SetValue(SIGNAL_SECONDS);
            pn.PlaceList.add(duration);
            pn.PlaceList.add(FuzzyModelSupport.transfer("TL_out" + lane,
                    firstLanePort + lane - 1, "P_TL" + lane));
        }
        pn.ConstantPlaceList.add(FuzzyModelSupport.fuzzyPlace("Red", -1F));
        pn.ConstantPlaceList.add(FuzzyModelSupport.fuzzyPlace("Green", 0F));
        pn.ConstantPlaceList.add(FuzzyModelSupport.fuzzyPlace("Yellow", 1F));

        PetriTransition initialize = phase(pn, "t_initialize", "Initialize", "AllRed", 0);
        for (int lane = 1; lane <= 4; lane++) signal(initialize, lane, "Red");

        PetriTransition laneOneGreen = phase(pn, "t_green1", "AllRed", "Lane1Green", SIGNAL_SECONDS);
        signal(laneOneGreen, 1, "Green");
        for (int lane = 1; lane <= 4; lane++) {
            String green = "Lane" + lane + "Green";
            String yellow = "Lane" + lane + "Yellow";
            String duration = "d" + lane;
            PetriTransition startYellow = new PetriTransition(pn) {
                @Override
                public void BookTokens() throws CloneNotSupportedException {
                    super.BookTokens();
                    // The scheduler snapshots Delay immediately after booking the green token.
                    DataInteger setting = (DataInteger) util.GetPetriObjectByName(duration, Parent.PlaceList);
                    Delay = setting.Value;
                }
            };
            phase(startYellow, "t_yellow" + lane, green, yellow, SIGNAL_SECONDS);
            startYellow.GuardMappingList.get(0).condition.SetNextCondition(LogicConnector.AND,
                    new Condition(startYellow, duration, TransitionCondition.NotNull));
            signal(startYellow, lane, "Yellow");
            if (lane < 4) {
                int next = lane + 1;
                PetriTransition startGreen = phase(pn, "t_green" + next + "_after" + lane,
                        yellow, "Lane" + next + "Green", SIGNAL_SECONDS);
                signal(startGreen, lane, "Red");
                signal(startGreen, next, "Green");
            }
        }
        PetriTransition allRed = phase(pn, "t_all_red_after4", "Lane4Yellow", "AllRed", SIGNAL_SECONDS);
        signal(allRed, 4, "Red");
        return pn;
    }

    private static PetriTransition phase(PetriNet pn, String name, String from, String to, int delay) {
        return phase(new PetriTransition(pn), name, from, to, delay);
    }

    private static PetriTransition phase(PetriTransition transition, String name, String from, String to, int delay) {
        transition.TransitionName = name;
        transition.InputPlaceName.add(from);
        GuardMapping guard = new GuardMapping();
        guard.condition = FuzzyModelSupport.allNotNull(transition, from);
        guard.Activations.add(new Activation(transition, FuzzyModelSupport.moveRules(),
                FuzzyModelSupport.fuzzyInputs(from), TransitionOperation.FLRS, FuzzyModelSupport.outputs(to)));
        transition.GuardMappingList.add(guard);
        transition.Delay = delay;
        transition.Parent.Transitions.add(transition);
        return transition;
    }

    private static void signal(PetriTransition transition, int lane, String color) {
        transition.GuardMappingList.get(0).Activations.add(new Activation(transition,
                color, TransitionOperation.SendOverNetwork, "TL_out" + lane));
    }

    public static void main(String[] args) {
        int firstLanePort = args.length > 0 ? Integer.parseInt(args[0]) : FIRST_LANE_PORT;
        int controllerPort = args.length > 1 ? Integer.parseInt(args[1]) : NETWORK_PORT;
        FuzzyModelSupport.show(build(firstLanePort, controllerPort));
    }
}
