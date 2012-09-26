package testcase;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

//import com.esotericsoftware.kryo.Kryo;
//import com.esotericsoftware.kryo.io.Input;
import state.EndState;
import state.GlobalState;
import state.SequenceStateGenerator;
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
import event.eventtype.PrimaryEventType;
import java.io.BufferedInputStream;

import org.apache.commons.math3.distribution.BinomialDistribution;
import org.apache.commons.math3.distribution.IntegerDistribution;
import org.apache.commons.math3.distribution.PoissonDistribution;

public class TestSequenceState {		
	/**
	 * @param args
	 * @throws FileNotFoundException
	 * @throws ParseException
	 */
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
		
		String predicate = "E1.a1 + E2.a2 < 5";
		long timeDuration = 5l;
		
		Evaluator evaluator = JaninoEvalFactory.fromString(new ComplexEventType(seqList), predicate);
		SequenceStateGenerator seqGenerator = new SequenceStateGenerator();
		State finalState = seqGenerator.getSequenceState(seqList, evaluator, timeDuration);
		
		EndState endState = new EndState();
		globalState.registerInputEventClassToState(finalState.getOutputEventClass(), endState);
		
		//EventClass[] eventClasses = new EventClass[generator.getNumEventClasses()];
		//for(int i=0;i<generator.eventConfigurations.size();i++) {
		/*for(int i=0;i<2;i++) {
			RandomEventConfiguration config=generator.eventConfigurations.get(i);
			eventClasses[i]=config.eClass;
		}
		ConcurrentState state = new ConcurrentState(5,"E1.a1+E2.a2 < 5",eventClasses);
		*/
		List<Event> generatedEvents = new LinkedList<Event>();
		long start=System.nanoTime();
		long time=0;
		
		for(int i=0;i<testcases;i++) {
			//PrimaryEvent e = kryo.readObject(input, PrimaryEvent.class);
			Event e = generator.generateEvent();
			time+=timeStampDist.sample();
			e.setTimeStamp(new IntervalTimeStamp(time,time));
			System.out.println(i+":"+e);
			//System.out.println(state.submitNext(e));	
			//state.submitNext(e);
			globalState.submitNext(e);
			endState.getGeneratedEvents(generatedEvents);
			System.out.println(generatedEvents);
			generatedEvents.clear();
		
				
			/*for(ComplexEvent ce : state.submitNext(e)) {
				if(!ce.isConsumed()) {
					System.out.println(ce);
					ce.setConsumed(true);
				}
			}*/
		}
		System.out.println((System.nanoTime()-start)/1000000.0);
			
	}
}
