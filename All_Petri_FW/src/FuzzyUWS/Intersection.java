package FuzzyUWS;
import Components.*;
import Enumerations.TransitionOperation;
public class Intersection {
    public static final float[] WEIGHTS={0.25F,0.50F,0.12F,0.12F};
    public static PetriNet build() {
        PetriNet n=ModelSupport.net("Intersection",1100);
        for(String name:new String[]{"P_i1","P_i2","P_i3","P_i4","Pair12","Pair34","P_I"})
            ModelSupport.place(n,name,null,false);
        for(int i=1;i<=4;i++) {
            ModelSupport.place(n,"P_o"+i,null,false);
            ModelSupport.place(n,"Weight"+i,WEIGHTS[i-1],true);
            ModelSupport.transfer(n,"ToExit"+i,1110+i,"P_o");
        }
        ModelSupport.flrs(ModelSupport.transition(n,"t1","P_i1","P_i2"),ModelSupport.adder(),"Pair12","P_i1","P_i2");
        ModelSupport.flrs(ModelSupport.transition(n,"t2","P_i3","P_i4"),ModelSupport.adder(),"Pair34","P_i3","P_i4");
        ModelSupport.flrs(ModelSupport.transition(n,"t3","Pair12","Pair34"),ModelSupport.adder(),"P_I","Pair12","Pair34");
        PetriTransition split=ModelSupport.transition(n,"t_split","P_I");
        for(int i=1;i<=4;i++) {
            split.GuardMappingList.get(0).Activations.add(new Activation(ModelSupport.inputs("P_I","Weight"+i),
                TransitionOperation.Prod_Fuzzy,"P_o"+i,split));
            ModelSupport.send(ModelSupport.transition(n,"t_out"+i,"P_o"+i),"P_o"+i,"ToExit"+i);
        }
        return n;
    }
    public static void main(String[] args) { ModelSupport.window(build()); }
}
