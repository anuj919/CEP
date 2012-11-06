package testcase.batch;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import state.ConcurrentStateGeneratorWithReuse;
import state.EndState;
import state.GlobalState;
import state.State;
import testdatagenerator.parser.ParseException;
import time.timestamp.IntervalTimeStamp;
import time.timestamp.TimeStamp;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;

import event.Event;
import event.EventClass;
import event.PrimaryEvent;
import event.eventtype.PrimaryEventType;

public class TestConcurrentStateWithAutomaton {		
	/**
	 * @param args
	 * @throws FileNotFoundException
	 * @throws ParseException
	 */
	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws FileNotFoundException, ParseException {
		long start=System.nanoTime();
		int repeat=1;
		
		if(args.length!=4) {
			System.err.println("Usage: java -cp <> TestConcurrentStateWithAutomaton specification-file-path #testcases #eventclasses predicate duration-for-window");
			System.exit(1);
		}
		
		String inputFilePath = args[0];
		int testcases = Integer.parseInt(args[1]);
		String eventClasses = args[2];
		String predicate = args[3];
		long timeDuration = Integer.parseInt(args[4]);

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
		
		GlobalState globalState = GlobalState.getInstance();
		
		for(EventClass ec : classes)
			globalState.registerEventClass(ec);
		
		
		List<EventClass> seqList = new ArrayList<EventClass>();
		StringTokenizer tokenizer = new StringTokenizer(eventClasses, " ");
		while(tokenizer.hasMoreTokens())
			seqList.add(globalState.getEventClass(tokenizer.nextToken()));
		
		ConcurrentStateGeneratorWithReuse concGenerator = new ConcurrentStateGeneratorWithReuse();
		State lastState = concGenerator.generateConcurrentState(seqList, predicate, timeDuration);
		
		EndState endState = new EndState();
		globalState.registerInputEventClassToState(lastState.getOutputEventClass(), endState);
		System.out.println("Done setting up the automaton..");
		
		List<Event> generatedEveList = new LinkedList<Event>();
		int batchSize = 1000;
		List<PrimaryEvent> currentBatch = new LinkedList<PrimaryEvent>();
		long generatedEvents=0;
		long prev=0,i=0;
		TimeStamp lastts=new IntervalTimeStamp(-1l, -1l);
		int eventFlushPeriod=300;
		
		
		for(int j=0;j<repeat;j++)
		while(i<testcases) {
			currentBatch.clear();
			for(int k=0;k<batchSize;k++)
				currentBatch.add(kryo.readObjectOrNull(input, PrimaryEvent.class));
			
			for(int k=0;k<batchSize && i<testcases;k++,i++) {
				if(i%(testcases/100) == 0)
					System.out.println(i+" events injected");
				Event e = currentBatch.get(k);
				
				if(i%eventFlushPeriod==0) {
					globalState.submitHeartbeat(lastts);
				}
				lastts=e.getTimeStamp();
				
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
