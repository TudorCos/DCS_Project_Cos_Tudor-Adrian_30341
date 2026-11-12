package FuzzyUWU;

import java.util.ArrayList;

import Components.Condition;
import Components.PetriNet;
import Components.PetriNetWindow;
import Components.PetriTransition;
import DataObjects.DataFuzzy;
import DataObjects.DataString;
import DataObjects.DataTransfer;
import DataOnly.FLRS;
import DataOnly.FV;
import DataOnly.Fuzzy;
import DataOnly.PlaceNameWithWeight;
import DataOnly.TransferOperation;
import Enumerations.FZ;
import Enumerations.LogicConnector;
import Enumerations.TransitionCondition;

final class FuzzyModelSupport {
	private FuzzyModelSupport() {
	}

	static FLRS moveRules() {
		return new FLRS(new FV(FZ.NL), new FV(FZ.NM), new FV(FZ.ZR), new FV(FZ.PM), new FV(FZ.PL));
	}

	static FLRS additionRules() {
		return new FLRS(
				new FV(FZ.NL), new FV(FZ.NL), new FV(FZ.NL), new FV(FZ.NM), new FV(FZ.ZR),
				new FV(FZ.NL), new FV(FZ.NL), new FV(FZ.NM), new FV(FZ.ZR), new FV(FZ.PM),
				new FV(FZ.NL), new FV(FZ.NM), new FV(FZ.ZR), new FV(FZ.PM), new FV(FZ.PL),
				new FV(FZ.NM), new FV(FZ.ZR), new FV(FZ.PM), new FV(FZ.PL), new FV(FZ.PL),
				new FV(FZ.ZR), new FV(FZ.PM), new FV(FZ.PL), new FV(FZ.PL), new FV(FZ.PL));
	}

	static DataFuzzy fuzzyPlace(String name) {
		return fuzzyPlace(name, null);
	}

	static DataFuzzy fuzzyPlace(String name, Float value) {
		DataFuzzy place = new DataFuzzy();
		place.SetName(name);
		if (value != null)
			place.SetValue(new Fuzzy(value));
		return place;
	}

	static DataString marker(String name, String value) {
		DataString marker = new DataString();
		marker.SetName(name);
		if (value != null)
			marker.SetValue(value);
		return marker;
	}

	static DataTransfer transfer(String name, int port, String targetPlace) {
		DataTransfer transfer = new DataTransfer();
		transfer.SetName(name);
		transfer.Value = new TransferOperation("localhost", Integer.toString(port), targetPlace);
		return transfer;
	}

	static ArrayList<PlaceNameWithWeight> fuzzyInputs(String... placeNames) {
		ArrayList<PlaceNameWithWeight> inputs = new ArrayList<>();
		for (String placeName : placeNames)
			inputs.add(new PlaceNameWithWeight(placeName, 1F));
		return inputs;
	}

	static ArrayList<String> outputs(String... placeNames) {
		ArrayList<String> outputs = new ArrayList<>();
		for (String placeName : placeNames)
			outputs.add(placeName);
		return outputs;
	}

	static Condition allNotNull(PetriTransition transition, String... placeNames) {
		Condition first = null;
		Condition previous = null;
		for (String placeName : placeNames) {
			Condition current = new Condition(transition, placeName, TransitionCondition.NotNull);
			if (first == null)
				first = current;
			if (previous != null)
				previous.SetNextCondition(LogicConnector.AND, current);
			previous = current;
		}
		return first;
	}

	static void show(PetriNet petriNet) {
		PetriNetWindow frame = new PetriNetWindow(false);
		frame.petriNet = petriNet;
		frame.setVisible(true);
	}
}
