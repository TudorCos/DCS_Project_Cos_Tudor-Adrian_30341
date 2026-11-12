package FuzzyUWU;

import java.util.ArrayList;

import Components.Activation;
import Components.GuardMapping;
import Components.PetriNet;
import Components.PetriTransition;
import DataObjects.DataFuzzy;
import DataObjects.DataTransfer;
import DataOnly.FLRS;
import DataOnly.PlaceNameWithWeight;
import Enumerations.TransitionOperation;

public class IntersectionModel {
	public static final int NETWORK_PORT = 1080;
	public static final int FIRST_EXIT_PORT = 1091;

	public static PetriNet build(int networkPort, int firstExitPort) {
		PetriNet pn = new PetriNet();
		pn.PetriNetName = "Fuzzy four-way intersection";
		pn.SetName("Intersection");
		pn.NetworkPort = networkPort;
		pn.Delay = 250;

		for (int lane = 1; lane <= 4; lane++)
			pn.PlaceList.add(FuzzyModelSupport.fuzzyPlace("P_i" + lane));

		pn.PlaceList.add(FuzzyModelSupport.fuzzyPlace("P_i12"));
		pn.PlaceList.add(FuzzyModelSupport.fuzzyPlace("P_i34"));
		pn.PlaceList.add(FuzzyModelSupport.fuzzyPlace("P_i"));

		for (int lane = 1; lane <= 4; lane++) {
			pn.PlaceList.add(FuzzyModelSupport.fuzzyPlace("P_o" + lane));
			DataTransfer transfer = FuzzyModelSupport.transfer("P_out" + lane, firstExitPort + lane - 1,
					"P_o" + lane);
			pn.PlaceList.add(transfer);
		}

		pn.ConstantPlaceList.add(FuzzyModelSupport.fuzzyPlace("Split25", 0.25F));
		pn.ConstantPlaceList.add(FuzzyModelSupport.fuzzyPlace("Split50", 0.50F));
		pn.ConstantPlaceList.add(FuzzyModelSupport.fuzzyPlace("Split12A", 0.12F));
		pn.ConstantPlaceList.add(FuzzyModelSupport.fuzzyPlace("Split12B", 0.12F));

		FLRS add = FuzzyModelSupport.additionRules();
		addTransition(pn, "t1", add, "P_i1", "P_i2", "P_i12");
		addTransition(pn, "t2", add, "P_i3", "P_i4", "P_i34");
		addTransition(pn, "t3", add, "P_i12", "P_i34", "P_i");

		PetriTransition split = new PetriTransition(pn);
		split.TransitionName = "t_s";
		split.InputPlaceName.add("P_i");
		GuardMapping splitGuard = new GuardMapping();
		splitGuard.condition = FuzzyModelSupport.allNotNull(split, "P_i");
		splitGuard.Activations.add(product(split, "Split25", "P_o1"));
		splitGuard.Activations.add(product(split, "Split50", "P_o2"));
		splitGuard.Activations.add(product(split, "Split12A", "P_o3"));
		splitGuard.Activations.add(product(split, "Split12B", "P_o4"));
		split.GuardMappingList.add(splitGuard);
		pn.Transitions.add(split);

		for (int lane = 1; lane <= 4; lane++)
			addOutputTransition(pn, lane);

		return pn;
	}

	private static void addTransition(PetriNet pn, String name, FLRS rules, String input1, String input2,
			String output) {
		PetriTransition transition = new PetriTransition(pn);
		transition.TransitionName = name;
		transition.InputPlaceName.add(input1);
		transition.InputPlaceName.add(input2);
		GuardMapping guard = new GuardMapping();
		guard.condition = FuzzyModelSupport.allNotNull(transition, input1, input2);
		guard.Activations.add(new Activation(transition, rules, FuzzyModelSupport.fuzzyInputs(input1, input2),
				TransitionOperation.FLRS, FuzzyModelSupport.outputs(output)));
		transition.GuardMappingList.add(guard);
		pn.Transitions.add(transition);
	}

	private static Activation product(PetriTransition transition, String constant, String output) {
		ArrayList<PlaceNameWithWeight> inputs = FuzzyModelSupport.fuzzyInputs("P_i", constant);
		return new Activation(inputs, TransitionOperation.Prod_Fuzzy, output, transition);
	}

	private static void addOutputTransition(PetriNet pn, int lane) {
		String output = "P_o" + lane;
		String transfer = "P_out" + lane;
		PetriTransition transition = new PetriTransition(pn);
		transition.TransitionName = "t_out" + lane;
		transition.InputPlaceName.add(output);
		GuardMapping guard = new GuardMapping();
		guard.condition = FuzzyModelSupport.allNotNull(transition, output);
		guard.Activations.add(new Activation(transition, output, TransitionOperation.SendOverNetwork, transfer));
		transition.GuardMappingList.add(guard);
		pn.Transitions.add(transition);
	}

	public static void main(String[] args) {
		int networkPort = args.length > 0 ? Integer.parseInt(args[0]) : NETWORK_PORT;
		int firstExitPort = args.length > 1 ? Integer.parseInt(args[1]) : FIRST_EXIT_PORT;
		FuzzyModelSupport.show(build(networkPort, firstExitPort));
	}
}
