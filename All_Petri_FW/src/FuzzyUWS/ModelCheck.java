package FuzzyUWS;
import Components.*;
import DataObjects.*;
import DataOnly.Fuzzy;
public class ModelCheck {
    private static int checks;
    static DataFuzzy place(PetriNet n,String name) {
        return (DataFuzzy)n.util.GetPetriObjectByName(name,n.PlaceList);
    }
    static void set(PetriNet n,String name,float value) { place(n,name).SetValue(new Fuzzy(value)); }
    static PetriTransition transition(PetriNet n,String name) {
        return n.Transitions.stream().filter(t->t.TransitionName.equals(name)).findFirst().get();
    }
    static void fire(PetriNet n,String name) throws Exception {
        PetriTransition t=transition(n,name);
        if(!t.CheckConditions()) throw new AssertionError("Disabled: "+name);
        t.BookTokens(); t.Activate();
    }
    static void equal(float expected,float actual) {
        if(!Float.isFinite(actual)||Math.abs(expected-actual)>0.0001F) throw new AssertionError(expected+" != "+actual);
        checks++;
    }
    static void quiet(PetriNet n) { n.ShowLog=false; n.ShowLogInWindow=false; }
    public static void main(String[] args) throws Exception {
        for(int lane=1;lane<=4;lane++) {
            PetriNet n=lane==2?EntranceLane2.build(true):EntranceLane.build(lane,true); quiet(n);
            fire(n,"t_in"); fire(n,"t_send");
            for(float signal:new float[]{-1F,1F}) {
                set(n,"P_TL",signal);
                if(transition(n,"t_out").CheckConditions()) throw new AssertionError("Passed non-green");
                checks++;
            }
            set(n,"P_TL",0F); fire(n,"t_out");
            equal(0.1F,place(n,"P_out").Value.Value); equal(0F,place(n,"P_TL").Value.Value);
        }
        PetriNet n=Intersection.build(); quiet(n);
        for(float input:new float[]{0.1F,0F}) {
            for(int i=1;i<=4;i++) set(n,"P_i"+i,input);
            fire(n,"t1"); fire(n,"t2"); fire(n,"t3");
            equal(4*input,place(n,"P_I").Value.Value); fire(n,"t_split");
            for(int i=1;i<=4;i++) {
                float product=place(n,"P_o"+i).Value.Value;
                equal(4*input*Intersection.WEIGHTS[i-1],product);
                PetriNet exit=ExitLane.build(i); quiet(exit); set(exit,"P_o",product); fire(exit,"t_exit");
                equal(Math.round(product*200)/200F,place(exit,i==2?"ExitFlow":"P_Exit").Value.Value);
            }
        }
        PetriNet controller=Controller.build();
        for(int i=0;i<8;i++) equal(4,transition(controller,"Advance"+i).Delay);
        System.out.println("Passed "+checks+" model checks (no network sends or GUI).");
    }
}
