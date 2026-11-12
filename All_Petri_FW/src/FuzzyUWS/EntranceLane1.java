package FuzzyUWS;

/** Dedicated runnable model for entrance lane 1. */
public class EntranceLane1 {
    public static void main(String[] args) {
        ModelSupport.window(EntranceLane.build(1, args.length > 0 && args[0].equals("demo")));
    }
}
