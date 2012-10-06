package testcase.stream;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import state.ConcurrentState;
import state.ConcurrentStateGenerator;
import state.EndState;
import state.GlobalState;
import state.State;
import testdatagenerator.parser.ParseException;
import time.timestamp.IntervalTimeStamp;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import event.Event;
import event.EventClass;
import event.PrimaryEvent;
import event.eventtype.PrimaryEventType;

public class TestConcurrentStateWithAutomaton {

	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws ParseException, IOException {
		if(args.length!=3) {
			System.err.println("Arguments: <eventclasses> <predicate> <duration>");
			System.exit(1);
		}
		
		String eventClasses = args[0];
		String predicate = args[1];
		long timeDuration = Integer.parseInt(args[2]);
		
		Kryo kryo = new Kryo();
		kryo.register(PrimaryEvent.class);
		kryo.register(IntervalTimeStamp.class);
		kryo.register(PrimaryEventType.class);
		kryo.register(EventClass.class);
		kryo.register(HashMap.class);
		kryo.register(LinkedList.class);

	
		
		String ipAddress = "127.0.0.1";
		int port=5555;
		Socket socket=null;
		try {
			socket = new Socket(ipAddress, port);
			socket.setTcpNoDelay(true);
		} catch (UnknownHostException e2) {
			e2.printStackTrace();
			System.err.println("Unknown Host");
			System.exit(1);
		} catch (IOException e2) {
			System.err.println("Problem connecting to server");
			System.exit(2);
		}
		

		Input input = new Input(socket.getInputStream());
		Output output = new Output(socket.getOutputStream());
		
		LinkedList<EventClass> classes = kryo.readObject(input, LinkedList.class);
		System.out.println("Got event classes");
		GlobalState globalState = GlobalState.getInstance();
		for(EventClass ec : classes)
			globalState.registerEventClass(ec);
		
		List<EventClass> seqList = new ArrayList<EventClass>();
		StringTokenizer tokenizer = new StringTokenizer(eventClasses, " ");
		while(tokenizer.hasMoreTokens())
			seqList.add(globalState.getEventClass(tokenizer.nextToken()));
		
		ConcurrentStateGenerator concGenerator = new ConcurrentStateGenerator();
		State lastState = concGenerator.generateConcurrentState(seqList, predicate, timeDuration);
		
		EndState endState = new EndState();
		globalState.registerInputEventClassToState(lastState.getOutputEventClass(), endState);
		System.out.println("Done setting up the automaton..");
		
		Integer rate=kryo.readObject(input, Integer.class);
		System.out.println("Received Rate="+rate);
		
		int queueSize=3000;
		EventStreamReader eventReader=new EventStreamReader(kryo, input, queueSize,rate);
		BlockingQueue<PrimaryEvent> queue=eventReader.getInputQueue();
		new Thread(eventReader).start();
		
		List<Event> generatedEveList = new LinkedList<Event>();
		long generatedEvents=0;
		long prev=0;

		kryo.writeObject(output, "GoAhead");
		output.flush();
                
		for(int i=0;;i++)
		{			
			if(i%1000 == 0) {
				System.out.println("Injected: "+i+" Generated: "+generatedEvents);
			}
			Event e;
			long t1=System.nanoTime();
			try {
				e = queue.poll(1,TimeUnit.SECONDS);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
				break;
			}
			if(e==null)
				break;
			
			//e=kryo.readObject(input, PrimaryEvent.class);
			long t2=System.nanoTime();
			//System.out.println(e);
			
			globalState.submitNext(e);
			endState.getGeneratedEvents(generatedEveList);
			long t3=System.nanoTime();
			System.err.println("Dequeue time: "+(t2-t1)+" Processing time: "+(t3-t2) );
			generatedEvents+=generatedEveList.size();
			//if(generatedEvents-prev>1000) {
			//	System.out.println("****"+generatedEvents+" events generated****");
			//	prev=generatedEvents;
			//}
			//if(generatedEveList.size()>0)
			//	System.out.println("*******"+generatedEveList+"*******");
			generatedEveList.clear();
		}	
	}
}
