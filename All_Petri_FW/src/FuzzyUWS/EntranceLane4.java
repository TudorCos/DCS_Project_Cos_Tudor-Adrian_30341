package FuzzyUWS;

/** Dedicated runnable model for entrance lane 4. */
public class EntranceLane4 {
    public static void main(String[] args) {
        ModelSupport.window(EntranceLane.build(4, args.length > 0 && args[0].equals("demo")));
    }
}
