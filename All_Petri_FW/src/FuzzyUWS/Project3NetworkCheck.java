package FuzzyUWS;

import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.ServerSocket;
import Components.PetriNet;
import DataObjects.DataFuzzy;
import DataOnly.Fuzzy;
import Utilities.DataOverNetwork;

/** Runs real framework schedulers and localhost transfers; use with GUI models stopped. */
public class Project3NetworkCheck {
    static void send(int port,String name,float value) throws Exception {
        try(Socket socket=new Socket("localhost",port);
            ObjectOutputStream out=new ObjectOutputStream(socket.getOutputStream())) {
            DataFuzzy p=new DataFuzzy(); p.SetName(name); p.SetValue(new Fuzzy(value));
            DataOverNetwork message=new DataOverNetwork(); message.petriObject=p; message.NetWorkPort=port;
            out.writeObject(message);
        }
    }
    static void await(PetriNet n,String place,float expected) throws Exception {
        long deadline=System.nanoTime()+10_000_000_000L;
        while(System.nanoTime()<deadline) {
            DataFuzzy p=ModelCheck.place(n,place);
            if(p.Value!=null && Math.abs(p.Value.Value-expected)<0.0001F) return;
            Thread.sleep(20);
        }
        throw new AssertionError(n.PetriNetName+"."+place+" expected "+expected+", got "+ModelCheck.place(n,place));
    }
    public static void main(String[] args) throws Exception {
        PetriNet round=Roundabout.build(), lane=EntranceLane2.build(false), alley=Alley.build(), intersection=Intersection.build();
        PetriNet e1=ExitLane.build(1),e2=ExitLane2.build(),e3=ExitLane.build(3),e4=ExitLane.build(4);
        PetriNet[] nets={round,lane,alley,intersection,e1,e2,e3,e4};
        // Fail before starting if an existing GUI process owns any required port.
        for(PetriNet n:nets) try(ServerSocket probe=new ServerSocket(n.NetworkPort)) { }
        for(PetriNet n:nets) {
            ModelCheck.quiet(n); n.Delay=50;
            Thread worker=new Thread(n); worker.setDaemon(true); worker.start();
        }
        Thread.sleep(800);
        send(1130,"P_i1",0.3F); send(1130,"P_i2",0.1F); send(1130,"P_i3",0.1F);
        send(1131,"P_i",0.1F);
        await(round,"P_out1",0.165F); await(round,"P_out2",0.165F);
        await(lane,"Lane",0.265F); // Still red: roundabout and alley deliveries merged.
        send(1100,"P_i1",0F); send(1100,"P_i3",0F); send(1100,"P_i4",0F);
        send(1102,"P_TL",0F);
        await(e1,"P_Exit",0.065F); await(e3,"P_Exit",0.03F); await(e4,"P_Exit",0.03F);
        await(round,"L1",0.13F); // Exit lane 2 has delivered back into the roundabout.
        System.out.println("PASS: real localhost roundabout -> lane2, alley -> lane2, lane2 -> intersection,");
        System.out.println("intersection -> four exits, exit2 -> roundabout. Outputs: 0.065, 0.03, 0.03; return L1=0.13.");
        // Framework listener threads have no close API; this isolated check process exits here.
        System.exit(0);
    }
}
