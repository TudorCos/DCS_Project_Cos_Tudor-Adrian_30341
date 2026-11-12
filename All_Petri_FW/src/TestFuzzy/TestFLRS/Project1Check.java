package TestFuzzy.TestFLRS;

import java.util.ArrayList;
import Components.Activation;
import Components.PetriNet;
import Components.PetriTransition;
import DataObjects.DataFuzzy;
import DataOnly.FLRS;
import DataOnly.Fuzzy;
import DataOnly.PlaceNameWithWeight;
import Enumerations.TransitionOperation;

/** Runs the actual framework inference without windows or network listeners. */
public class Project1Check {
    private static int checks;

    private static float evaluate(FLRS rules, float x1, Float x2, float w2)
            throws CloneNotSupportedException {
        PetriNet net = new PetriNet();
        net.ShowLog = false;
        net.ShowLogInWindow = false;
        PetriTransition transition = new PetriTransition(net);
        ArrayList<PlaceNameWithWeight> inputs = new ArrayList<>();
        DataFuzzy first = new DataFuzzy();
        first.SetName("P1");
        first.SetValue(new Fuzzy(x1));
        transition.TempMarking.add(first);
        inputs.add(new PlaceNameWithWeight("P1", 1F));
        if (x2 != null) {
            DataFuzzy second = new DataFuzzy();
            second.SetName("P2");
            second.SetValue(new Fuzzy(x2));
            transition.TempMarking.add(second);
            inputs.add(new PlaceNameWithWeight("P2", w2));
        }
        DataFuzzy output = new DataFuzzy();
        output.SetName("OUT");
        net.PlaceList.add(output);
        ArrayList<String> outputs = new ArrayList<>();
        outputs.add("OUT");
        new Activation(transition, rules, inputs, TransitionOperation.FLRS, outputs).Activate();
        return ((DataFuzzy) net.PlaceList.get(0)).Value.Value;
    }

    private static void check(float expected, float actual) {
        if (!Float.isFinite(actual) || Math.abs(expected - actual) > 0.0001F)
            throw new AssertionError("Expected " + expected + ", got " + actual);
        checks++;
    }

    public static void main(String[] args) throws CloneNotSupportedException {
        float[] samples = {-1F, -0.75F, -0.5F, -0.1F, 0F, 0.1F, 0.2F, 0.5F, 0.75F, 1F};
        for (float x : samples) {
            check(x, evaluate(FLRS1X1.identityRules(), x, null, 1F));
            for (float y : samples) {
                check(y, evaluate(FLRS2X1.readerRules(), x, y, 1F));
                check(-y, evaluate(FLRS2X1.readerRules(), x, y, -1F));
            }
        }
        System.out.println("Passed " + checks + " inference checks.");
        System.out.println("1x1: P1=0.1 -> P2=" + evaluate(FLRS1X1.identityRules(), 0.1F, null, 1F));
        System.out.println("Reader: P1=0.1, P2=0.2, w2=-1 -> P3="
                + evaluate(FLRS2X1.readerRules(), 0.1F, 0.2F, -1F));
        System.out.println("Reader: P1=0.1, P2=0.2, w2=+1 -> P3="
                + evaluate(FLRS2X1.readerRules(), 0.1F, 0.2F, 1F));
    }
}
