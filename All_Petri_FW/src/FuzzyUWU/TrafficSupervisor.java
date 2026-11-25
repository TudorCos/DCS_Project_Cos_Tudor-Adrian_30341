package FuzzyUWU;

import static Enumerations.TransitionCondition.Equal;
import static Enumerations.TransitionCondition.NotEqual;

import Components.Activation;
import Components.Condition;
import Components.GuardMapping;
import Components.PetriNet;
import Components.PetriTransition;
import DataObjects.DataInteger;
import Enumerations.LogicConnector;
import Enumerations.TransitionCondition;
import Enumerations.TransitionOperation;

public class TrafficSupervisor {
    public static final int NETWORK_PORT = 1101;
    private static final int LANE_COUNT = 4;

    public static PetriNet build(int supervisorPort, int controllerPort) {
        PetriNet net = new PetriNet();
        net.SetName("TrafficSupervisor");
        net.PetriNetName = "OETPN traffic supervisor";
        net.NetworkPort = supervisorPort;
        net.Delay = 250;
        net.ConstantPlaceList.add(FuzzyModelSupport.fuzzyPlace("PL", 1F));
        for (int duration : new int[] {2, 4, 5, 6, 8}) {
            DataInteger constant = new DataInteger();
            constant.SetName("Seconds" + duration);
            constant.SetValue(duration);
            net.ConstantPlaceList.add(constant);
        }

        PetriTransition calculate = new PetriTransition(net);
        calculate.TransitionName = "T0";
        calculate.Delay = 0;
        net.Transitions.add(calculate);
        for (int lane = 1; lane <= LANE_COUNT; lane++) {
            String flow = "f" + lane;
            String duration = "d" + lane;
            String output = "OP" + lane;
            net.PlaceList.add(FuzzyModelSupport.fuzzyPlace(flow));
            DataInteger durationPlace = new DataInteger();
            durationPlace.SetName(duration);
            net.PlaceList.add(durationPlace);
            net.PlaceList.add(FuzzyModelSupport.transfer(output, controllerPort, duration));
            calculate.InputPlaceName.add(flow);

            PetriTransition send = new PetriTransition(net);
            send.TransitionName = "T" + lane;
            send.Delay = 0;
            send.InputPlaceName.add(duration);
            GuardMapping mapping = new GuardMapping();
            mapping.condition = FuzzyModelSupport.allNotNull(send, duration);
            mapping.Activations.add(new Activation(send, duration, TransitionOperation.SendOverNetwork, output));
            send.GuardMappingList.add(mapping);
            net.Transitions.add(send);
        }

        // No jam.
        addSubguard(calculate, NotEqual, NotEqual, NotEqual, NotEqual, 5, 5, 5, 5);

        // One lane jammed
        addSubguard(calculate, Equal,    NotEqual, NotEqual, NotEqual, 8, 4, 4, 4);
        addSubguard(calculate, NotEqual, Equal,    NotEqual, NotEqual, 4, 8, 4, 4);
        addSubguard(calculate, NotEqual, NotEqual, Equal,    NotEqual, 4, 4, 8, 4);
        addSubguard(calculate, NotEqual, NotEqual, NotEqual, Equal,    4, 4, 4, 8);

        // Two lanes jammed
        addSubguard(calculate, Equal,    Equal,    NotEqual, NotEqual, 6, 6, 4, 4);
        addSubguard(calculate, NotEqual, Equal,    Equal,    NotEqual, 4, 6, 6, 4);
        addSubguard(calculate, NotEqual, NotEqual, Equal,    Equal,    4, 4, 6, 6);
        addSubguard(calculate, Equal,    NotEqual, NotEqual, Equal,    6, 4, 4, 6);
        addSubguard(calculate, NotEqual, Equal,    NotEqual, Equal,    4, 6, 4, 6);
        addSubguard(calculate, Equal,    NotEqual, Equal,    NotEqual, 6, 4, 6, 4);

        // Three lanes jammed
        addSubguard(calculate, Equal,    Equal,    Equal,    NotEqual, 6, 6, 6, 2);
        addSubguard(calculate, Equal,    Equal,    NotEqual, Equal,    6, 6, 2, 6);
        addSubguard(calculate, Equal,    NotEqual, Equal,    Equal,    6, 2, 6, 6);
        addSubguard(calculate, NotEqual, Equal,    Equal,    Equal,    2, 6, 6, 6);

        // All lanes jammed
        addSubguard(calculate, Equal,    Equal,    Equal,    Equal,    5, 5, 5, 5);
        return net;
    }

    private static void addSubguard(PetriTransition t0, TransitionCondition lane1, TransitionCondition lane2,
            TransitionCondition lane3, TransitionCondition lane4, int d1, int d2, int d3, int d4) {
        Condition f1 = new Condition(t0, "f1", lane1, "PL");
        Condition f2 = new Condition(t0, "f2", lane2, "PL");
        Condition f3 = new Condition(t0, "f3", lane3, "PL");
        Condition f4 = new Condition(t0, "f4", lane4, "PL");

        f3.SetNextCondition(LogicConnector.AND, f4);
        f2.SetNextCondition(LogicConnector.AND, f3);
        f1.SetNextCondition(LogicConnector.AND, f2);

        GuardMapping mapping = new GuardMapping();
        mapping.condition = f1;
        mapping.Activations.add(new Activation(t0, "Seconds" + d1, TransitionOperation.Move, "d1"));
        mapping.Activations.add(new Activation(t0, "Seconds" + d2, TransitionOperation.Move, "d2"));
        mapping.Activations.add(new Activation(t0, "Seconds" + d3, TransitionOperation.Move, "d3"));
        mapping.Activations.add(new Activation(t0, "Seconds" + d4, TransitionOperation.Move, "d4"));
        t0.GuardMappingList.add(mapping);
    }

    public static void main(String[] args) {
        int supervisorPort = args.length > 0 ? Integer.parseInt(args[0]) : NETWORK_PORT;
        int controllerPort = args.length > 1 ? Integer.parseInt(args[1]) : TrafficLightController.NETWORK_PORT;
        FuzzyModelSupport.show(build(supervisorPort, controllerPort));
    }
}
