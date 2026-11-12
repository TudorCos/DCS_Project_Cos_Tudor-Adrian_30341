package FuzzyUWS;
import Components.PetriNet;
/** External alley flow into lane 2's P_st. */
public class Alley {
    public static PetriNet build() {
        PetriNet n=ModelSupport.net("Alley ST",1131);
        ModelSupport.place(n,"P_i",null,false);
        ModelSupport.transfer(n,"P_out",1102,"P_st");
        ModelSupport.send(ModelSupport.transition(n,"t_send","P_i"),"P_i","P_out");
        return n;
    }
    public static void main(String[] args) { ModelSupport.window(build()); }
}
