package FuzzyUWS;
import Components.*;
/** Sequential G1,Y1,G2,Y2,G3,Y3,G4,Y4 phases; other lanes remain red. */
public class Controller {
    public static PetriNet build() {
        PetriNet n=ModelSupport.net("Traffic light controller",1120);
        ModelSupport.place(n,"Red",-1F,true); ModelSupport.place(n,"Green",0F,true);
        ModelSupport.place(n,"Yellow",1F,true);
        ModelSupport.place(n,"Boot",0F,false);
        for(int i=0;i<8;i++) ModelSupport.place(n,"Phase"+i,null,false);
        for(int i=1;i<=4;i++) ModelSupport.transfer(n,"Light"+i,1100+i,"P_TL");
        PetriTransition boot=ModelSupport.transition(n,"Initialize","Boot");
        ModelSupport.flrs(boot,ModelSupport.identity(),"Phase0","Boot");
        signals(boot,0);
        for(int i=0;i<8;i++) {
            int next=(i+1)%8;
            PetriTransition t=ModelSupport.transition(n,"Advance"+i,"Phase"+i);
            // Framework counts down 4,3,2,1,0: five one-second ticks.
            t.Delay=4;
            ModelSupport.flrs(t,ModelSupport.identity(),"Phase"+next,"Phase"+i);
            signals(t,next);
        }
        return n;
    }
    private static void signals(PetriTransition t,int phase) {
        for(int lane=1;lane<=4;lane++) ModelSupport.send(t,
            lane==phase/2+1?(phase%2==0?"Green":"Yellow"):"Red","Light"+lane);
    }
    public static void main(String[] args) { ModelSupport.window(build()); }
}
