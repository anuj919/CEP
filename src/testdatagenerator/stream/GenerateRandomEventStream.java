package testdatagenerator.stream;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

import org.apache.commons.math3.distribution.IntegerDistribution;
import org.apache.commons.math3.distribution.RealDistribution;

import testdatagenerator.parser.ConfigFileParser;
import testdatagenerator.parser.ParseException;
import testdatagenerator.parser.RandomEventConfiguration;
import time.timestamp.IntervalTimeStamp;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import event.AttributeType;
import event.EventClass;
import event.PrimaryEvent;
import event.eventtype.PrimaryEventType;
import event.util.TypeMismatchException;

/*
 * This class reads the configuration file, and generates event streams
 * accordingly
 */

public class GenerateRandomEventStream {
	Reader reader;
	List<RandomEventConfiguration> eventConfigurations;
	List<EventClass> eventClasses;
	PriorityQueue<RandomEventConfiguration> heap;
	
	public GenerateRandomEventStream(String fileName,int rate) throws FileNotFoundException, ParseException {
		//this.eventDistconfig = eventDistconfig;
		double relativeTotalRate=0;
		reader = new BufferedReader(new FileReader(fileName));
		eventConfigurations = new ConfigFileParser(reader).getEventConfig();
		eventClasses = new LinkedList<EventClass>();
		for(RandomEventConfiguration config: eventConfigurations) {
			eventClasses.add(config.eClass);
			relativeTotalRate+=config.relativeRate;
		}
		for(RandomEventConfiguration config: eventConfigurations) {
			config.actualRate=config.relativeRate * rate / relativeTotalRate;
		}
		
		// populate heap
		heap = new PriorityQueue<RandomEventConfiguration>(eventConfigurations.size(), new Comparator<RandomEventConfiguration>() {
			@Override
			public int compare(RandomEventConfiguration o1,
					RandomEventConfiguration o2) {
				if(o1.time < o2.time)
					return -1;
				else if(o1.time == o2.time)
					return 0;
				else
					return 1;
			}
		});
		for(RandomEventConfiguration config: eventConfigurations) {
			heap.add(config);
		}
	}
	
	public int getNumEventClasses() {
		return eventConfigurations.size();
	}
	
	public List<EventClass> getEventClasses() {
		return eventClasses;
	}
	
	public PrimaryEvent generateEvent() {
		RandomEventConfiguration selectedConfig = heap.poll();
		PrimaryEvent event = generateEvent(selectedConfig);
		heap.offer(selectedConfig);
		return event;
	}
	
	private PrimaryEvent generateEvent(RandomEventConfiguration selected) { 
		EventClass eClass = selected.eClass; 
		PrimaryEventType et = (PrimaryEventType) eClass.getEventType();
		
		PrimaryEvent pe = new PrimaryEvent(eClass);
		//self.time+= - log(self.r.rvs())/self.rate
		double timestampDiff = - Math.log(Math.random())/selected.actualRate;
		selected.time+=timestampDiff;
		pe.setTimeStamp(new IntervalTimeStamp(selected.time, selected.time));
		
		try {
			for(String attName : et.getAttributeNames()) {
				AttributeType atype = et.getAttributeType(attName);
				switch(atype) {
				case Integer : 
					IntegerDistribution iDist = (IntegerDistribution)selected.map.get(attName).distConfig.distribution;
					pe.addAttributeValue(attName, iDist.sample());
					break;
				case Double:
					RealDistribution dDist = (RealDistribution)selected.map.get(attName).distConfig.distribution;
					pe.addAttributeValue(attName, dDist.sample());
					break;
				case String:
					IntegerDistribution sDist = (IntegerDistribution)selected.map.get(attName).distConfig.distribution;
					String[] values = selected.map.get(attName).strValues;
					pe.addAttributeValue(attName, values[sDist.sample()]);
					break;
				default:
					throw new RuntimeException("Not supported type");
				}
			}
		} catch(TypeMismatchException te) {
			te.printStackTrace(); // can not happen
		}
		return pe;
	}
	
	public static void main(String[] args) throws ParseException, UnknownHostException, IOException {
		String inputFilePath = "spec.txt";
		if(args.length<1 || args.length>2) {
			System.err.println("Arguments: <event-rate>");
			System.exit(1);
		}
		int rate=Integer.parseInt(args[0]);
		int testcases=Integer.MAX_VALUE;
		if(args.length==2) {
			testcases=Integer.parseInt(args[1]);
		}
		long timePerEventInns=1000000000/rate;
		
		GenerateRandomEventStream generator = new GenerateRandomEventStream(inputFilePath,rate);
				
		int port=5555;		
		ServerSocket socket = new ServerSocket(port);
		
		try{
		// accepting one connection only
		//for(;;) {
			System.out.println("Waiting for connections...");
			Socket clientSocket = socket.accept();
			clientSocket.setTcpNoDelay(true);
			System.out.println("Got clinet connection...");

			Kryo kryo = new Kryo();
			kryo.register(PrimaryEvent.class);
			kryo.register(IntervalTimeStamp.class);
			kryo.register(PrimaryEventType.class);
			kryo.register(EventClass.class);
			kryo.register(HashMap.class);
			kryo.register(LinkedList.class);
			
			Output output = new Output(clientSocket.getOutputStream());
			Input input = new Input(clientSocket.getInputStream());			
			kryo.writeObject(output, generator.eventClasses);
			output.flush();
			System.out.println("Sent eventclasses");
			
			kryo.writeObject(output, new Integer(rate));
			output.flush();
			
			String goAhead = kryo.readObject(input, String.class);
			long time=0;
			long startEvent,endEvent;
			int BATCH_SIZE=100;
			ArrayList<PrimaryEvent> list=new ArrayList<PrimaryEvent>(BATCH_SIZE);
			
			long startTime=System.nanoTime();
			int i=0;
			for(;i<testcases;) {
				startEvent=System.nanoTime();
				//Event e = queue.take();
				
				for(int j=0;j<BATCH_SIZE && i<testcases;j++,i++) {
					PrimaryEvent e=generator.generateEvent();
					list.add(e);
				}
				//System.out.println("Time to take event from queue:"+(System.nanoTime()-startEvent));
				//System.out.println(e);
				
				
				
				try{
					kryo.writeObject(output, list);
					output.flush();
				} catch(KryoException ke) {
					System.out.println("Client "+clientSocket.getRemoteSocketAddress() +"disconnecting...");
					break;
				}
				
				if(i%1000==0)
					System.out.println(i+" Events generated..");
				endEvent=System.nanoTime();
				//System.out.println("Time to send an event:"+(endEvent-startEvent));
				long sleepTime=(timePerEventInns*BATCH_SIZE-(endEvent-startEvent))/1000000;
				System.err.println("TimeFor100events="+timePerEventInns*100/1000+"Consumed time for 100 events="+(endEvent-startEvent)/1000+" SleepTime= "+sleepTime);
				if(sleepTime<0) {
					System.out.println((endEvent-startEvent)+"<"+timePerEventInns*BATCH_SIZE+ " so not sleeping");
					list.clear();
					continue;
				}
				
				Thread.sleep(sleepTime);
				list.clear();
			}
			System.out.println("Generated "+i+" events in "+(System.nanoTime()-startTime)/1000000+"ms");
//		}
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			socket.close();
		}		
	}
	
}
