package FuzzyUWS;

import Components.*;
import Enumerations.TransitionCondition;

/** Project 3: roundabout flow plus alley flow, then traffic-light gating. */
public class EntranceLane2 {
    public static PetriNet build(boolean demo) {
        PetriNet n=ModelSupport.net("Entrance lane 2 - roundabout and alley",1102);
        ModelSupport.place(n,"P_R",demo?0.1F:null,false);
        ModelSupport.place(n,"P_st",demo?0F:null,false);
        for(String name:new String[]{"P_l","Lane","P_out"}) ModelSupport.place(n,name,null,false);
        ModelSupport.place(n,"P_TL",-1F,false);
        ModelSupport.place(n,"Green",0F,true);
        ModelSupport.transfer(n,"ToIntersection",1100,"P_i2");
        ModelSupport.flrs(ModelSupport.transition(n,"t_in","P_R","P_st"),ModelSupport.adder(),"P_l","P_R","P_st");
        ModelSupport.flrs(ModelSupport.transition(n,"t_send","P_l"),ModelSupport.identity(),"Lane","P_l");
        PetriTransition out=ModelSupport.transition(n,"t_out","Lane");
        ModelSupport.require(out,new Condition(out,"P_TL",TransitionCondition.Equal,"Green"));
        ModelSupport.flrs(out,ModelSupport.identity(),"P_out","Lane");
        ModelSupport.send(ModelSupport.transition(n,"t_network","P_out"),"P_out","ToIntersection");
        return n;
    }
    public static void main(String[] args) {
        ModelSupport.window(build(args.length>0 && args[0].equals("demo")));
    }
}
