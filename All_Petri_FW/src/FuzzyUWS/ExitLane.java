package FuzzyUWS;
import Components.PetriNet;
public class ExitLane {
    public static PetriNet build(int lane) {
        if (lane == 2) return ExitLane2.build();
        PetriNet n=ModelSupport.net("Exit lane "+lane,1110+lane);
        ModelSupport.place(n,"P_o",null,false); ModelSupport.place(n,"P_Exit",null,false);
        ModelSupport.flrs(ModelSupport.transition(n,"t_exit","P_o"),ModelSupport.identity(),"P_Exit","P_o");
        return n;
    }
    public static void main(String[] args) { ModelSupport.window(build(ModelSupport.lane(args))); }
}
