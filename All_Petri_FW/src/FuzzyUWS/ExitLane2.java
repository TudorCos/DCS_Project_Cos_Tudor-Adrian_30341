package FuzzyUWS;
import Components.PetriNet;
/** Project 3: exit lane 2 now feeds the roundabout, not a terminal sink. */
public class ExitLane2 {
    public static PetriNet build() {
        PetriNet n=ModelSupport.net("Exit lane 2 - to roundabout",1112);
        ModelSupport.place(n,"P_o",null,false);
        ModelSupport.place(n,"ExitFlow",null,false);
        ModelSupport.transfer(n,"P_Exit",1130,"P_i1");
        // FLRS outputs must be DataFuzzy in this framework, so stage before sending.
        ModelSupport.flrs(ModelSupport.transition(n,"t_exit","P_o"),ModelSupport.identity(),"ExitFlow","P_o");
        ModelSupport.send(ModelSupport.transition(n,"t_network","ExitFlow"),"ExitFlow","P_Exit");
        return n;
    }
    public static void main(String[] args) { ModelSupport.window(build()); }
}
