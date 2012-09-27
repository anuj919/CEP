package testcase;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.math3.distribution.BinomialDistribution;
import org.apache.commons.math3.distribution.IntegerDistribution;
import org.apache.commons.math3.distribution.PoissonDistribution;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;

import state.ConcurrentStateGeneratorNoReuse;
import state.EndState;
import state.GlobalState;
import state.State;
import testdatagenerator.GenerateRandomEvents;
import testdatagenerator.parser.ParseException;
import time.timestamp.IntervalTimeStamp;
import evaluator.Evaluator;
import evaluator.JaninoEvalFactory;
import event.Event;
import event.EventClass;
import event.PrimaryEvent;
import event.eventtype.ComplexEventType;
//import com.esotericsoftware.kryo.Kryo;
//import com.esotericsoftware.kryo.io.Input;
import event.eventtype.PrimaryEventType;

public class TestConcurrentStateWithAutomatonNoReuse {		
	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws FileNotFoundException, ParseException {
		int repeat=1;
		//String specFilePath = "spec.txt";
		//GenerateRandomEvents generator = new GenerateRandomEvents(specFilePath);
		//IntegerDistribution dist = new BinomialDistribution(generator.getNumEventClasses()-1,0.2);
		//generator.setDistribution(dist);
		
		String inputFilePath = args[0];
		int testcases = Integer.parseInt(args[1]);
		Kryo kryo = new Kryo();
		Input input = new Input(new BufferedInputStream(new FileInputStream(inputFilePath)));
		kryo.register(PrimaryEvent.class);
		kryo.register(IntervalTimeStamp.class);
		kryo.register(PrimaryEventType.class);
		kryo.register(EventClass.class);
		kryo.register(HashMap.class);
		kryo.register(LinkedList.class);
		
		LinkedList<EventClass> classes = kryo.readObject(input, LinkedList.class);
		int maxTestCasesInFile = kryo.readObject(input, Integer.class);
		if(testcases > maxTestCasesInFile) {
			System.out.println("Data file contains only"+maxTestCasesInFile+" testcases");
			return;
		}
		
		
		//List<EventClass> classes = generator.getEventClasses();
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
		
		Evaluator evaluator = JaninoEvalFactory.fromString(new ComplexEventType(seqList), predicate);
		ConcurrentStateGeneratorNoReuse concGenerator = new ConcurrentStateGeneratorNoReuse();
		State lastState = concGenerator.generateConcurrentState(seqList, evaluator, timeDuration);
		
		EndState endState = new EndState();
		globalState.registerInputEventClassToState(lastState.getOutputEventClass(), endState);
		System.out.println("Done setting up the automaton..");
		
		List<Event> generatedEveList = new LinkedList<Event>();
		int batchSize = 1000;
		List<PrimaryEvent> currentBatch = new LinkedList<PrimaryEvent>();
		long generatedEvents=0;
		long prev=0,i=0;
		
		long start=System.nanoTime();
		
		for(int j=0;j<repeat;j++)
		while(i<testcases) {
			currentBatch.clear();
			for(int k=0;k<batchSize;k++)
				currentBatch.add(kryo.readObjectOrNull(input, PrimaryEvent.class));
			
			for(int k=0;k<batchSize && i<testcases;k++,i++) {
				if(i%(testcases/100) == 0)
					System.out.println(i+" events injected");
				Event e = currentBatch.get(k);
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
		}
		System.out.println((System.nanoTime()-start)/1000000.0/repeat);
		System.out.println("****"+generatedEvents+" events generated****");
	}
}
