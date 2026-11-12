package FuzzyUWU;

import java.util.ArrayList;

import Components.PetriNet;

public class UrbanVehicleFlowSystem {
	public static void main(String[] args) {
		float[] initialFlows = { 0.8F, 0.6F, 0.4F, 0.2F };
		ArrayList<PetriNet> nets = new ArrayList<>();

		for (int lane = 1; lane <= 4; lane++)
			nets.add(ExitLaneModel.build(lane, IntersectionModel.FIRST_EXIT_PORT + lane - 1,
					RoundaboutModel.NETWORK_PORT));
		nets.add(RoundaboutModel.build(RoundaboutModel.NETWORK_PORT, LaneModel.FIRST_LANE_PORT + 1, 0.4F, 0.2F));
		nets.add(IntersectionModel.build(IntersectionModel.NETWORK_PORT, IntersectionModel.FIRST_EXIT_PORT));
		for (int lane = 1; lane <= 4; lane++)
			nets.add(LaneModel.build(lane, LaneModel.FIRST_LANE_PORT + lane - 1, LaneModel.INTERSECTION_PORT,
					lane == 2 ? null : initialFlows[lane - 1]));
		nets.add(TrafficLightController.build(TrafficLightController.FIRST_LANE_PORT));
		nets.add(AlleyStreetModel.build(LaneModel.FIRST_LANE_PORT + 1, initialFlows[1]));

		for (PetriNet net : nets) {
			Thread thread = new Thread(net, net.PetriNetName);
			thread.start();
		}

		System.out.println("Fuzzy urban vehicle-flow system started.");
		System.out.println("Incoming lanes: 1081-1084, intersection: 1080, exit lanes: 1091-1094.");
		System.out.println("Roundabout: 1100; Exit Lane 2 -> roundabout P_i1 -> Lane 2 P_R.");
	}
}
