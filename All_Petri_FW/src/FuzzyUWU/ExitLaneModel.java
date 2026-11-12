package FuzzyUWU;

import Components.Activation;
import Components.GuardMapping;
import Components.PetriNet;
import Components.PetriTransition;
import DataObjects.DataTransfer;
import DataOnly.FLRS;
import Enumerations.TransitionOperation;

public class ExitLaneModel {
	public static final int FIRST_EXIT_PORT = 1091;

	public static PetriNet build(int laneNumber, int networkPort) {
		return build(laneNumber, networkPort, RoundaboutModel.NETWORK_PORT);
	}

	public static PetriNet build(int laneNumber, int networkPort, int roundaboutPort) {
		if (laneNumber < 1 || laneNumber > 4)
			throw new IllegalArgumentException("laneNumber must be between 1 and 4");

		PetriNet pn = new PetriNet();
		pn.PetriNetName = "Fuzzy exit lane " + laneNumber;
		pn.SetName("ExitLane" + laneNumber);
		pn.NetworkPort = networkPort;
		pn.Delay = 250;

		String input = "P_o" + laneNumber;
		String output = "P_Exit" + laneNumber;
		pn.PlaceList.add(FuzzyModelSupport.fuzzyPlace(input));
		if (laneNumber == 2) {
			DataTransfer transfer = FuzzyModelSupport.transfer(output, roundaboutPort, "P_i1");
			pn.PlaceList.add(transfer);
		} else {
			pn.PlaceList.add(FuzzyModelSupport.fuzzyPlace(output));
		}

		FLRS move = FuzzyModelSupport.moveRules();
		PetriTransition transition = new PetriTransition(pn);
		transition.TransitionName = "t_exit" + laneNumber;
		transition.InputPlaceName.add(input);

		GuardMapping guard = new GuardMapping();
		guard.condition = FuzzyModelSupport.allNotNull(transition, input);
		if (laneNumber == 2) {
			guard.Activations.add(new Activation(transition, input, TransitionOperation.SendOverNetwork, output));
		} else {
			guard.Activations.add(new Activation(transition, move, FuzzyModelSupport.fuzzyInputs(input),
					TransitionOperation.FLRS, FuzzyModelSupport.outputs(output)));
		}
		transition.GuardMappingList.add(guard);
		pn.Transitions.add(transition);

		return pn;
	}

	public static void main(String[] args) {
		int laneNumber = args.length > 0 ? Integer.parseInt(args[0]) : 1;
		int networkPort = args.length > 1 ? Integer.parseInt(args[1]) : FIRST_EXIT_PORT + laneNumber - 1;
		int roundaboutPort = args.length > 2 ? Integer.parseInt(args[2]) : RoundaboutModel.NETWORK_PORT;
		FuzzyModelSupport.show(build(laneNumber, networkPort, roundaboutPort));
	}
}
