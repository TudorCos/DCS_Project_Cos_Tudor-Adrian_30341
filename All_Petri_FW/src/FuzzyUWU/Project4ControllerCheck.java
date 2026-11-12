package FuzzyUWU;

import java.util.ArrayList;
import java.util.List;
import Components.PetriNet;
import Components.PetriTransition;
import DataObjects.DataFuzzy;
import DataObjects.DataInteger;
import DataObjects.DataTransfer;
import DataOnly.Fuzzy;
import Interfaces.PetriObject;

/** Exercises real transition activations while capturing traffic-light messages. */
public class Project4ControllerCheck {
    private static final class RecordedTransfer extends DataTransfer {
        final List<Float> values = new ArrayList<>();
        @Override public void SetValue(Object value) {
            values.add(((DataFuzzy)value).Value.Value);
        }
    }
    private static PetriTransition transition(PetriNet net,String name) {
        return net.Transitions.stream().filter(t->t.TransitionName.equals(name)).findFirst().get();
    }
    private static void fire(PetriNet net,String name) throws Exception {
        PetriTransition t=transition(net,name);
        if(!t.CheckConditions()) throw new AssertionError(name+" was disabled");
        t.BookTokens(); t.Activate();
    }
    private static void expect(RecordedTransfer[] outputs,int[] signals) {
        for(int lane=0;lane<4;lane++) {
            List<Float> values=outputs[lane].values;
            if(values.isEmpty() || values.get(values.size()-1)!=signals[lane])
                throw new AssertionError("Lane "+(lane+1)+" expected "+signals[lane]+", got "+values);
        }
    }
    public static void main(String[] args) throws Exception {
        PetriNet net=TrafficLightController.build(1081);
        net.ShowLog=false; net.ShowLogInWindow=false;
        RecordedTransfer[] outputs=new RecordedTransfer[4];
        for(int lane=1;lane<=4;lane++) {
            int index=net.util.GetIndexByName("TL_out"+lane,net.PlaceList);
            DataTransfer original=(DataTransfer)net.PlaceList.get(index);
            RecordedTransfer recorder=new RecordedTransfer();
            recorder.SetName(original.GetName()); recorder.Value=original.Value;
            net.PlaceList.set(index,recorder); outputs[lane-1]=recorder;
            if(!original.Value.Port.equals(Integer.toString(1080+lane)) ||
               !original.Value.TargetPlaceName.equals("P_TL"+lane)) throw new AssertionError("Wrong lane destination");
            DataInteger d=(DataInteger)net.util.GetPetriObjectByName("d"+lane,net.ConstantPlaceList);
            if(d.Value!=5) throw new AssertionError("Missing five-second duration");
        }
        fire(net,"t_initialize"); expect(outputs,new int[]{-1,-1,-1,-1});
        fire(net,"t_green1"); expect(outputs,new int[]{0,-1,-1,-1});
        for(int lane=1;lane<=4;lane++) {
            if(transition(net,"t_yellow"+lane).Delay!=5) throw new AssertionError("Initial delay");
            fire(net,"t_yellow"+lane);
            int[] yellow={-1,-1,-1,-1}; yellow[lane-1]=1; expect(outputs,yellow);
            if(lane==2) {
                DataInteger d=(DataInteger)net.util.GetPetriObjectByName("d2",net.ConstantPlaceList);
                d.SetValue(7);
                // DynamicDelay reads at firing; repeat the transition on the next cycle below.
            }
            int next=lane==4?1:lane+1;
            fire(net,"t_green"+next+"_after"+lane);
            int[] green={-1,-1,-1,-1}; green[next-1]=0; expect(outputs,green);
        }
        fire(net,"t_yellow1"); fire(net,"t_green2_after1"); fire(net,"t_yellow2");
        if(transition(net,"t_yellow2").Delay!=7) throw new AssertionError("DynamicDelay ignored d2");
        System.out.println("Controller phases, all four signals, destinations, cycle, and dynamic delay passed.");
    }
}
