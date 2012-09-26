package testcase;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.math3.distribution.BinomialDistribution;
import org.apache.commons.math3.distribution.IntegerDistribution;
import org.apache.commons.math3.distribution.PoissonDistribution;

import state.ConcurrentState;
import state.EndState;
import state.GlobalState;
import testdatagenerator.GenerateRandomEvents;
import testdatagenerator.parser.ParseException;
import time.timestamp.IntervalTimeStamp;
import event.Event;
import event.EventClass;
//import com.esotericsoftware.kryo.Kryo;
//import com.esotericsoftware.kryo.io.Input;

public class TestConcurrentState {		
	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws FileNotFoundException, ParseException {
		int repeat=1;
		int testcases = 1000;
		String inputFilePath = "spec.txt";
		//String outputFilePath = "events.txt";
		GenerateRandomEvents generator = new GenerateRandomEvents(inputFilePath);
		IntegerDistribution dist = new BinomialDistribution(generator.getNumEventClasses()-1,0.2);
		IntegerDistribution timeStampDist = new PoissonDistribution(2);
		generator.setDistribution(dist);
		
		
		/*Kryo kryo = new Kryo();
		Input input = new Input(new BufferedInputStream(new FileInputStream(inputFilePath)));
		kryo.register(PrimaryEvent.class);
		kryo.register(IntervalTimeStamp.class);
		kryo.register(PrimaryEventType.class);
		kryo.register(EventClass.class);
		kryo.register(HashMap.class);
		kryo.register(LinkedList.class);*/
		
		//LinkedList<EventClass> classes = kryo.readObject(input, LinkedList.class);
		List<EventClass> classes = generator.getEventClasses();
		GlobalState globalState = GlobalState.getInstance();
		
		for(EventClass ec : classes)
			globalState.registerEventClass(ec);
		
		
		List<EventClass> seqList = new ArrayList<EventClass>();
		seqList.add(globalState.getEventClass("E1"));
		seqList.add(globalState.getEventClass("E2"));
		seqList.add(globalState.getEventClass("E3"));
		seqList.add(globalState.getEventClass("E4"));
        seqList.add(globalState.getEventClass("E5"));
        seqList.add(globalState.getEventClass("E6"));
        seqList.add(globalState.getEventClass("E7"));
		
		String predicate = "E1.a + E2.a < 5 && E3.a == E4.a";
		//String predicate = "E3.a + E4.a < 10 ";
		long timeDuration = 100l;
		//eventClasses[2]=globalState.getEventClass("E3");
		//eventClasses[3]=globalState.getEventClass("E4");
		ConcurrentState concState = new ConcurrentState(timeDuration,predicate,seqList);
		
		
		EndState endState = new EndState();
		globalState.registerInputEventClassToState(concState.getOutputEventClass(), endState);
		
		//EventClass[] eventClasses = new EventClass[generator.getNumEventClasses()];
		//for(int i=0;i<generator.eventConfigurations.size();i++) {
		/*for(int i=0;i<2;i++) {
			RandomEventConfiguration config=generator.eventConfigurations.get(i);
			eventClasses[i]=config.eClass;
		}
		ConcurrentState state = new ConcurrentState(5,"E1.a1+E2.a2 < 5",eventClasses);
		*/
                
		List<Event> generatedEveList = new LinkedList<Event>();
		//int batchSize = 1000;
		long time=0;
		long generatedEvents=0;
		long prev=0;
		
		long start=System.nanoTime();
		for(int j=0;j<repeat;j++)
			for(int i=0;i<testcases;i++) {
				if(i%(testcases/100) == 0)
					System.out.println(i+" events injected");
				Event e = generator.generateEvent();
				time+=timeStampDist.sample();
				e.setTimeStamp(new IntervalTimeStamp(time,time));
				//System.out.println(e);
				globalState.submitNext(e);
				endState.getGeneratedEvents(generatedEveList);
				generatedEvents+=generatedEveList.size();
				if(generatedEvents-prev>1000) {
					System.out.println("****"+generatedEvents+" events generated****");
					prev=generatedEvents;
				}
				//if(generatedEveList.size()>0)
				//	System.out.println("*******"+generatedEveList+"*******");
				generatedEveList.clear();
			}
		System.out.println((System.nanoTime()-start)/1000000.0/repeat + "ms");
		System.out.println("****"+generatedEvents+" events generated****");
			
	}
}
