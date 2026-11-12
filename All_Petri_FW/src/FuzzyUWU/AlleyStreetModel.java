package FuzzyUWU;

import Components.Activation;
import Components.GuardMapping;
import Components.PetriNet;
import Components.PetriTransition;
import DataObjects.DataTransfer;
import Enumerations.TransitionOperation;

public class AlleyStreetModel {
	public static PetriNet build(int lane2Port, Float initialFlow) {
		PetriNet pn = new PetriNet();
		pn.PetriNetName = "Fuzzy alley street";
		pn.SetName("AlleyStreet");
		pn.NetworkPort = 0;
		pn.Delay = 250;

		pn.PlaceList.add(FuzzyModelSupport.fuzzyPlace("P_i", initialFlow));
		DataTransfer output = FuzzyModelSupport.transfer("P_out", lane2Port, "P_st");
		pn.PlaceList.add(output);

		PetriTransition transition = new PetriTransition(pn);
		transition.TransitionName = "t_send";
		transition.InputPlaceName.add("P_i");
		GuardMapping guard = new GuardMapping();
		guard.condition = FuzzyModelSupport.allNotNull(transition, "P_i");
		guard.Activations.add(new Activation(transition, "P_i", TransitionOperation.SendOverNetwork, "P_out"));
		transition.GuardMappingList.add(guard);
		pn.Transitions.add(transition);

		return pn;
	}

	public static void main(String[] args) {
		int lane2Port = args.length > 0 ? Integer.parseInt(args[0]) : LaneModel.FIRST_LANE_PORT + 1;
		Float initialFlow = args.length > 1 ? Float.valueOf(args[1]) : 0.6F;
		FuzzyModelSupport.show(build(lane2Port, initialFlow));
	}
}
