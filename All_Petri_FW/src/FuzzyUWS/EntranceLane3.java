package FuzzyUWS;

/** Dedicated runnable model for entrance lane 3. */
public class EntranceLane3 {
    public static void main(String[] args) {
        ModelSupport.window(EntranceLane.build(3, args.length > 0 && args[0].equals("demo")));
    }
}
