package FuzzyUWS;

import java.util.ArrayList;
import java.util.Arrays;
import Components.*;
import DataObjects.*;
import DataOnly.*;
import Enumerations.*;

final class ModelSupport {
    private ModelSupport() {}
    static PetriNet net(String name, int port) {
        PetriNet n = new PetriNet(); n.PetriNetName=name; n.NetworkPort=port;
        n.Delay=1000; n.ShowLogInWindow=false; return n;
    }
    static DataFuzzy place(PetriNet n, String name, Float value, boolean constant) {
        DataFuzzy p=new DataFuzzy(); p.SetName(name);
        if(value!=null) p.SetValue(new Fuzzy(value));
        if(constant) n.ConstantPlaceList.add(p); else n.PlaceList.add(p);
        return p;
    }
    static FLRS identity() {
        return new FLRS(new FV(FZ.NL),new FV(FZ.NM),new FV(FZ.ZR),new FV(FZ.PM),new FV(FZ.PL));
    }
    static FLRS adder() {
        return new FLRS(
            new FV(FZ.NL),new FV(FZ.NL),new FV(FZ.NL),new FV(FZ.NM),new FV(FZ.ZR),
            new FV(FZ.NL),new FV(FZ.NL),new FV(FZ.NM),new FV(FZ.ZR),new FV(FZ.PM),
            new FV(FZ.NL),new FV(FZ.NM),new FV(FZ.ZR),new FV(FZ.PM),new FV(FZ.PL),
            new FV(FZ.NM),new FV(FZ.ZR),new FV(FZ.PM),new FV(FZ.PL),new FV(FZ.PL),
            new FV(FZ.ZR),new FV(FZ.PM),new FV(FZ.PL),new FV(FZ.PL),new FV(FZ.PL));
    }
    static ArrayList<PlaceNameWithWeight> inputs(String... names) {
        ArrayList<PlaceNameWithWeight> result=new ArrayList<>();
        for(String name:names) result.add(new PlaceNameWithWeight(name,1F));
        return result;
    }
    static PetriTransition transition(PetriNet n,String name,String... consumed) {
        PetriTransition t=new PetriTransition(n); t.TransitionName=name; t.Delay=0;
        t.InputPlaceName.addAll(Arrays.asList(consumed));
        GuardMapping g=new GuardMapping(); Condition last=null;
        for(String input:consumed) {
            Condition c=new Condition(t,input,TransitionCondition.NotNull);
            if(last==null) g.condition=c; else last.SetNextCondition(LogicConnector.AND,c);
            last=c;
        }
        t.GuardMappingList.add(g); n.Transitions.add(t); return t;
    }
    static void require(PetriTransition t, Condition c) {
        Condition last=t.GuardMappingList.get(0).condition;
        while(last.NextCondition!=null) last=last.NextCondition;
        last.SetNextCondition(LogicConnector.AND,c);
    }
    static void flrs(PetriTransition t,FLRS rules,String output,String... input) {
        t.GuardMappingList.get(0).Activations.add(new Activation(t,rules,inputs(input),
            TransitionOperation.FLRS,new ArrayList<>(Arrays.asList(output))));
    }
    static void transfer(PetriNet n,String name,int port,String target) {
        DataTransfer p=new DataTransfer(); p.SetName(name);
        p.Value=new TransferOperation("localhost",Integer.toString(port),target); n.PlaceList.add(p);
    }
    static void send(PetriTransition t,String input,String destination) {
        t.GuardMappingList.get(0).Activations.add(new Activation(t,input,TransitionOperation.SendOverNetwork,destination));
    }
    static void window(PetriNet n) {
        javax.swing.SwingUtilities.invokeLater(()->{
            PetriNetWindow w=new PetriNetWindow(false); w.petriNet=n; w.setVisible(true);
        });
    }
    static int lane(String[] args) {
        int lane=args.length==0?1:Integer.parseInt(args[0]);
        if(lane<1 || lane>4) throw new IllegalArgumentException("Lane must be 1..4");
        return lane;
    }
}
