package FuzzyUWS;
import Components.*;
import DataObjects.DataTransfer;
public class Project3Check {
    static void route(PetriNet n,String name,int port,String target) {
        DataTransfer transfer=(DataTransfer)n.util.GetPetriObjectByName(name,n.PlaceList);
        if(!transfer.Value.Port.equals(Integer.toString(port)) || !transfer.Value.TargetPlaceName.equals(target))
            throw new AssertionError("Wrong route: "+name);
    }
    public static void main(String[] args) throws Exception {
        PetriNet lane=EntranceLane2.build(false); ModelCheck.quiet(lane);
        ModelCheck.set(lane,"P_R",0.2F);
        if(ModelCheck.transition(lane,"t_in").CheckConditions()) throw new AssertionError("Missing alley must wait");
        ModelCheck.set(lane,"P_st",0.1F); ModelCheck.fire(lane,"t_in");
        ModelCheck.equal(0.3F,ModelCheck.place(lane,"P_l").Value.Value);
        ModelCheck.fire(lane,"t_send");
        if(ModelCheck.transition(lane,"t_out").CheckConditions()) throw new AssertionError("Red must block");
        ModelCheck.set(lane,"P_TL",0F); ModelCheck.fire(lane,"t_out");
        ModelCheck.equal(0.3F,ModelCheck.place(lane,"P_out").Value.Value);
        PetriNet exit=ExitLane2.build(); ModelCheck.quiet(exit);
        ModelCheck.set(exit,"P_o",0.3F); ModelCheck.fire(exit,"t_exit");
        route(exit,"P_Exit",1130,"P_i1");
        PetriNet round=Roundabout.build(); ModelCheck.quiet(round);
        // Simulate delivery at the verified network endpoint without opening sockets.
        ModelCheck.set(round,"P_i1",ModelCheck.place(exit,"ExitFlow").Value.Value);
        ModelCheck.set(round,"P_i2",0.1F); ModelCheck.set(round,"P_i3",0.1F);
        for(String t:new String[]{"t1","t2","t3","t_add12","t_add3"}) ModelCheck.fire(round,t);
        ModelCheck.equal(0.5F,ModelCheck.place(round,"L1L2L3").Value.Value);
        ModelCheck.fire(round,"t_split");
        for(int i=1;i<=3;i++) ModelCheck.equal(0.165F,ModelCheck.place(round,"E"+i).Value.Value);
        for(String t:new String[]{"t5","t6","t7"}) ModelCheck.fire(round,t);
        ModelCheck.equal(0.165F,ModelCheck.place(round,"P_out1").Value.Value);
        ModelCheck.equal(0.165F,ModelCheck.place(round,"P_out2").Value.Value);
        route(round,"P_out3",1102,"P_R"); route(Alley.build(),"P_out",1102,"P_st");
        ModelCheck.set(lane,"P_R",ModelCheck.place(round,"ReturnFlow").Value.Value);
        if(ModelCheck.transition(lane,"t_in").CheckConditions()) throw new AssertionError("Fresh alley token required");
        ModelCheck.set(lane,"P_st",0F); ModelCheck.fire(lane,"t_in");
        ModelCheck.equal(0.165F,ModelCheck.place(lane,"P_l").Value.Value);
        for(int i:new int[]{1,3,4}) {
            if(ModelCheck.place(EntranceLane.build(i,false),"P_i")==null || ModelCheck.place(ExitLane.build(i),"P_Exit")==null)
                throw new AssertionError("Other lane changed");
        }
        System.out.println("Project 3 checks passed: merge, gating, exit link, roundabout split, feedback, alley route, unchanged other lanes.");
    }
}
