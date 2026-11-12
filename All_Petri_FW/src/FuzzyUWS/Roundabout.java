package FuzzyUWS;
import Components.*;
import Enumerations.TransitionOperation;
/** Project 3: three incoming flows, three equal written shares, third returns to lane 2. */
public class Roundabout {
    public static PetriNet build() {
        PetriNet n=ModelSupport.net("Roundabout",1130);
        for(String name:new String[]{"P_i1","P_i2","P_i3","L1","L2","L3","L1L2","L1L2L3",
                "E1","E2","E3","P_out1","P_out2","ReturnFlow"})
            ModelSupport.place(n,name,null,false);
        ModelSupport.place(n,"Share",0.33F,true);
        ModelSupport.transfer(n,"P_out3",1102,"P_R");
        for(int i=1;i<=3;i++) ModelSupport.flrs(ModelSupport.transition(n,"t"+i,"P_i"+i),
            ModelSupport.identity(),"L"+i,"P_i"+i);
        ModelSupport.flrs(ModelSupport.transition(n,"t_add12","L1","L2"),ModelSupport.adder(),"L1L2","L1","L2");
        ModelSupport.flrs(ModelSupport.transition(n,"t_add3","L1L2","L3"),ModelSupport.adder(),"L1L2L3","L1L2","L3");
        PetriTransition split=ModelSupport.transition(n,"t_split","L1L2L3");
        for(int i=1;i<=3;i++) split.GuardMappingList.get(0).Activations.add(new Activation(
            ModelSupport.inputs("L1L2L3","Share"),TransitionOperation.Prod_Fuzzy,"E"+i,split));
        ModelSupport.flrs(ModelSupport.transition(n,"t5","E1"),ModelSupport.identity(),"P_out1","E1");
        ModelSupport.flrs(ModelSupport.transition(n,"t6","E2"),ModelSupport.identity(),"P_out2","E2");
        ModelSupport.flrs(ModelSupport.transition(n,"t7","E3"),ModelSupport.identity(),"ReturnFlow","E3");
        ModelSupport.send(ModelSupport.transition(n,"t_network","ReturnFlow"),"ReturnFlow","P_out3");
        return n;
    }
    public static void main(String[] args) { ModelSupport.window(build()); }
}
