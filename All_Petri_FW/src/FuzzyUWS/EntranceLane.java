package FuzzyUWS;

import Components.*;
import Enumerations.*;

/** One entrance process per lane. Controller signals remain available after gating. */
public class EntranceLane {
    public static PetriNet build(int lane, boolean demo) {
        if (lane == 2) return EntranceLane2.build(demo);
        PetriNet n=ModelSupport.net("Entrance lane "+lane,1100+lane);
        ModelSupport.place(n,"P_i",demo?0.1F:null,false);
        for(String name:new String[]{"P_l","Lane","P_out"}) ModelSupport.place(n,name,null,false);
        ModelSupport.place(n,"P_TL",-1F,false);
        ModelSupport.place(n,"Green",0F,true);
        ModelSupport.transfer(n,"ToIntersection",1100,"P_i"+lane);
        ModelSupport.flrs(ModelSupport.transition(n,"t_in","P_i"),ModelSupport.identity(),"P_l","P_i");
        ModelSupport.flrs(ModelSupport.transition(n,"t_send","P_l"),ModelSupport.identity(),"Lane","P_l");
        PetriTransition out=ModelSupport.transition(n,"t_out","Lane");
        ModelSupport.require(out,new Condition(out,"P_TL",TransitionCondition.Equal,"Green"));
        ModelSupport.flrs(out,ModelSupport.identity(),"P_out","Lane");
        ModelSupport.send(ModelSupport.transition(n,"t_network","P_out"),"P_out","ToIntersection");
        return n;
    }
    public static void main(String[] args) {
        ModelSupport.window(build(ModelSupport.lane(args),args.length>1 && args[1].equals("demo")));
    }
}
