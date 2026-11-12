package FuzzyUWU;

import java.util.ArrayList;

import Components.Activation;
import Components.GuardMapping;
import Components.PetriNet;
import Components.PetriTransition;
import DataObjects.DataTransfer;
import DataOnly.FLRS;
import DataOnly.PlaceNameWithWeight;
import Enumerations.TransitionOperation;

public class RoundaboutModel {
	public static final int NETWORK_PORT = 1100;

	public static PetriNet build(int networkPort, int lane2Port, Float localFlow2, Float localFlow3) {
		PetriNet pn = new PetriNet();
		pn.PetriNetName = "Fuzzy three-entry roundabout";
		pn.SetName("Roundabout");
		pn.NetworkPort = networkPort;
		pn.Delay = 250;

		pn.PlaceList.add(FuzzyModelSupport.fuzzyPlace("P_i1"));
		pn.PlaceList.add(FuzzyModelSupport.fuzzyPlace("P_i2", localFlow2));
		pn.PlaceList.add(FuzzyModelSupport.fuzzyPlace("P_i3", localFlow3));
		pn.PlaceList.add(FuzzyModelSupport.fuzzyPlace("L1"));
		pn.PlaceList.add(FuzzyModelSupport.fuzzyPlace("L2"));
		pn.PlaceList.add(FuzzyModelSupport.fuzzyPlace("L3"));
		pn.PlaceList.add(FuzzyModelSupport.fuzzyPlace("L1L2"));
		pn.PlaceList.add(FuzzyModelSupport.fuzzyPlace("L1L2L3"));
		pn.PlaceList.add(FuzzyModelSupport.fuzzyPlace("E1"));
		pn.PlaceList.add(FuzzyModelSupport.fuzzyPlace("E2"));
		pn.PlaceList.add(FuzzyModelSupport.fuzzyPlace("E3"));
		pn.PlaceList.add(FuzzyModelSupport.fuzzyPlace("P_out1"));
		pn.PlaceList.add(FuzzyModelSupport.fuzzyPlace("P_out2"));
		DataTransfer lane2Transfer = FuzzyModelSupport.transfer("P_out3", lane2Port, "P_R");
		pn.PlaceList.add(lane2Transfer);

		pn.ConstantPlaceList.add(FuzzyModelSupport.fuzzyPlace("Split33", 0.33F));

		FLRS move = FuzzyModelSupport.moveRules();
		FLRS add = FuzzyModelSupport.additionRules();
		addFuzzyTransition(pn, "t1", move, new String[] { "P_i1" }, "L1");
		addFuzzyTransition(pn, "t2", move, new String[] { "P_i2" }, "L2");
		addFuzzyTransition(pn, "t3", move, new String[] { "P_i3" }, "L3");
		addFuzzyTransition(pn, "t4", add, new String[] { "L1", "L2" }, "L1L2");
		addFuzzyTransition(pn, "t5", add, new String[] { "L1L2", "L3" }, "L1L2L3");

		PetriTransition split = new PetriTransition(pn);
		split.TransitionName = "t_s";
		split.InputPlaceName.add("L1L2L3");
		GuardMapping splitGuard = new GuardMapping();
		splitGuard.condition = FuzzyModelSupport.allNotNull(split, "L1L2L3");
		for (int exit = 1; exit <= 3; exit++)
			splitGuard.Activations.add(product(split, "E" + exit));
		split.GuardMappingList.add(splitGuard);
		pn.Transitions.add(split);

		addLocalOutputTransition(pn, "t6", "E1", "P_out1");
		addLocalOutputTransition(pn, "t7", "E2", "P_out2");
		addNetworkOutputTransition(pn, "t8", "E3", "P_out3");

		return pn;
	}

	private static void addFuzzyTransition(PetriNet pn, String name, FLRS rules, String[] inputs, String output) {
		PetriTransition transition = new PetriTransition(pn);
		transition.TransitionName = name;
		for (String input : inputs)
			transition.InputPlaceName.add(input);
		GuardMapping guard = new GuardMapping();
		guard.condition = FuzzyModelSupport.allNotNull(transition, inputs);
		guard.Activations.add(new Activation(transition, rules, FuzzyModelSupport.fuzzyInputs(inputs),
				TransitionOperation.FLRS, FuzzyModelSupport.outputs(output)));
		transition.GuardMappingList.add(guard);
		pn.Transitions.add(transition);
	}

	private static Activation product(PetriTransition transition, String output) {
		ArrayList<PlaceNameWithWeight> inputs = FuzzyModelSupport.fuzzyInputs("L1L2L3", "Split33");
		return new Activation(inputs, TransitionOperation.Prod_Fuzzy, output, transition);
	}

	private static void addLocalOutputTransition(PetriNet pn, String name, String input, String output) {
		PetriTransition transition = new PetriTransition(pn);
		transition.TransitionName = name;
		transition.InputPlaceName.add(input);
		GuardMapping guard = new GuardMapping();
		guard.condition = FuzzyModelSupport.allNotNull(transition, input);
		guard.Activations.add(new Activation(transition, input, TransitionOperation.Move, output));
		transition.GuardMappingList.add(guard);
		pn.Transitions.add(transition);
	}

	private static void addNetworkOutputTransition(PetriNet pn, String name, String input, String output) {
		PetriTransition transition = new PetriTransition(pn);
		transition.TransitionName = name;
		transition.InputPlaceName.add(input);
		GuardMapping guard = new GuardMapping();
		guard.condition = FuzzyModelSupport.allNotNull(transition, input);
		guard.Activations.add(new Activation(transition, input, TransitionOperation.SendOverNetwork, output));
		transition.GuardMappingList.add(guard);
		pn.Transitions.add(transition);
	}

	public static void main(String[] args) {
		int networkPort = args.length > 0 ? Integer.parseInt(args[0]) : NETWORK_PORT;
		int lane2Port = args.length > 1 ? Integer.parseInt(args[1]) : LaneModel.FIRST_LANE_PORT + 1;
		Float localFlow2 = args.length > 2 ? Float.valueOf(args[2]) : 0.4F;
		Float localFlow3 = args.length > 3 ? Float.valueOf(args[3]) : 0.2F;
		FuzzyModelSupport.show(build(networkPort, lane2Port, localFlow2, localFlow3));
	}
}
