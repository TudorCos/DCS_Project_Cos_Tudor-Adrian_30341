package FuzzyUWU;

import Components.Activation;
import Components.Condition;
import Components.GuardMapping;
import Components.PetriNet;
import Components.PetriTransition;
import DataObjects.DataFuzzy;
import DataObjects.DataTransfer;
import DataOnly.FLRS;
import Enumerations.LogicConnector;
import Enumerations.TransitionCondition;
import Enumerations.TransitionOperation;

public class LaneModel {
	public static final int INTERSECTION_PORT = 1080;
	public static final int FIRST_LANE_PORT = 1081;

	public static PetriNet build(int laneNumber, int networkPort, int intersectionPort, Float initialFlow) {
		return build(laneNumber, networkPort, intersectionPort, initialFlow, 0);
	}

	public static PetriNet build(int laneNumber, int networkPort, int intersectionPort, Float initialFlow,
			int supervisorPort) {
		if (laneNumber < 1 || laneNumber > 4)
			throw new IllegalArgumentException("laneNumber must be between 1 and 4");

		PetriNet pn = new PetriNet();
		pn.PetriNetName = "Fuzzy incoming lane " + laneNumber;
		pn.SetName("Lane" + laneNumber);
		pn.NetworkPort = networkPort;
		pn.Delay = 250;

		String suffix = Integer.toString(laneNumber);
		DataFuzzy inputFlow = laneNumber == 2
				? FuzzyModelSupport.fuzzyPlace("P_st", initialFlow)
				: FuzzyModelSupport.fuzzyPlace("P_i" + suffix, initialFlow);
		DataFuzzy roundaboutFlow = laneNumber == 2 ? FuzzyModelSupport.fuzzyPlace("P_R", 0F) : null;
		DataFuzzy lane = FuzzyModelSupport.fuzzyPlace("P_l" + suffix);
		DataFuzzy lane2 = FuzzyModelSupport.fuzzyPlace("lane2_" + suffix);
		DataFuzzy trafficLight = FuzzyModelSupport.fuzzyPlace("P_TL" + suffix);
		DataTransfer output = FuzzyModelSupport.transfer("P_out" + suffix, intersectionPort, "P_i" + suffix);

		pn.PlaceList.add(inputFlow);
		if (roundaboutFlow != null)
			pn.PlaceList.add(roundaboutFlow);
		pn.PlaceList.add(lane);
		pn.PlaceList.add(lane2);
		pn.PlaceList.add(trafficLight);
		pn.PlaceList.add(output);

		String laneInput = inputFlow.GetName();
		if (supervisorPort != 0) {
			String supervisorOutput = "Flow_to_supervisor" + suffix;
			laneInput = "Reported_flow" + suffix;
			pn.PlaceList.add(FuzzyModelSupport.fuzzyPlace(laneInput));
			pn.PlaceList.add(FuzzyModelSupport.transfer(supervisorOutput, supervisorPort, "f" + suffix));

			PetriTransition report = new PetriTransition(pn);
			report.TransitionName = "t_report" + suffix;
			report.Delay = 0;
			report.InputPlaceName.add(inputFlow.GetName());
			GuardMapping reportGuard = new GuardMapping();
			reportGuard.condition = FuzzyModelSupport.allNotNull(report, inputFlow.GetName());
			reportGuard.Activations.add(new Activation(report, inputFlow.GetName(), TransitionOperation.Move,
					laneInput));
			reportGuard.Activations.add(new Activation(report, inputFlow.GetName(), TransitionOperation.SendOverNetwork,
					supervisorOutput));
			report.GuardMappingList.add(reportGuard);
			pn.Transitions.add(report);
		}

		DataFuzzy green = FuzzyModelSupport.fuzzyPlace("Green", 0F);
		green.Printable = false;
		pn.ConstantPlaceList.add(green);

		FLRS move = FuzzyModelSupport.moveRules();
		FLRS laneInputRules = laneNumber == 2 ? FuzzyModelSupport.additionRules() : move;

		PetriTransition tIn = new PetriTransition(pn);
		tIn.TransitionName = "t_in" + suffix;
		tIn.InputPlaceName.add(laneInput);
		if (roundaboutFlow != null)
			tIn.InputPlaceName.add(roundaboutFlow.GetName());
		GuardMapping inGuard = new GuardMapping();
		inGuard.condition = roundaboutFlow == null
				? FuzzyModelSupport.allNotNull(tIn, laneInput)
				: FuzzyModelSupport.allNotNull(tIn, roundaboutFlow.GetName(), laneInput);
		inGuard.Activations.add(new Activation(tIn, laneInputRules,
				FuzzyModelSupport.fuzzyInputs(tIn.InputPlaceName.toArray(new String[0])),
				TransitionOperation.FLRS, FuzzyModelSupport.outputs(lane.GetName())));
		tIn.GuardMappingList.add(inGuard);
		pn.Transitions.add(tIn);

		PetriTransition tSend = new PetriTransition(pn);
		tSend.TransitionName = "t_send" + suffix;
		tSend.InputPlaceName.add(lane.GetName());
		GuardMapping sendGuard = new GuardMapping();
		sendGuard.condition = FuzzyModelSupport.allNotNull(tSend, lane.GetName());
		sendGuard.Activations.add(new Activation(tSend, move, FuzzyModelSupport.fuzzyInputs(lane.GetName()),
				TransitionOperation.FLRS, FuzzyModelSupport.outputs(lane2.GetName())));
		tSend.GuardMappingList.add(sendGuard);
		pn.Transitions.add(tSend);

		PetriTransition tOut = new PetriTransition(pn);
		tOut.TransitionName = "t_out" + suffix;
		tOut.InputPlaceName.add(lane2.GetName());
		tOut.InputPlaceName.add(trafficLight.GetName());

		Condition flowReady = new Condition(tOut, lane2.GetName(), TransitionCondition.NotNull);
		Condition greenLight = new Condition(tOut, trafficLight.GetName(), TransitionCondition.Equal, green.GetName());
		flowReady.SetNextCondition(LogicConnector.AND, greenLight);

		GuardMapping outGuard = new GuardMapping();
		outGuard.condition = flowReady;
		outGuard.Activations.add(new Activation(tOut, lane2.GetName(), TransitionOperation.SendOverNetwork,
				output.GetName()));
		outGuard.Activations.add(new Activation(tOut, trafficLight.GetName(), TransitionOperation.Move,
				trafficLight.GetName()));
		tOut.GuardMappingList.add(outGuard);
		pn.Transitions.add(tOut);

		return pn;
	}

	public static void main(String[] args) {
		int laneNumber = args.length > 0 ? Integer.parseInt(args[0]) : 1;
		int networkPort = args.length > 1 ? Integer.parseInt(args[1]) : FIRST_LANE_PORT + laneNumber - 1;
		int intersectionPort = args.length > 2 ? Integer.parseInt(args[2]) : INTERSECTION_PORT;
		Float initialFlow = args.length > 3 ? Float.valueOf(args[3]) : 0.8F;
		int supervisorPort = args.length > 4 ? Integer.parseInt(args[4]) : 0;
		FuzzyModelSupport.show(build(laneNumber, networkPort, intersectionPort, initialFlow, supervisorPort));
	}
}
