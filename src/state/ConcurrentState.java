package state;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import time.timemodel.TimeModel;
import time.timestamp.IntervalTimeStamp;
import time.timestamp.TimeStamp;
import evaluator.Evaluator;
import evaluator.JaninoEvalFactory;
import event.ComplexEvent;
import event.Event;
import event.EventClass;
import event.eventtype.ComplexEventType;
import event.eventtype.EventType;
import event.util.Policies;

/* This class implements OneState approach for Conjunction Query.
 */

public class ConcurrentState implements State {
	Map<EventClass, List<ComplexEvent> > map;
	EventClass outputEventClass;
	long duration;
	int numClasses;
	Evaluator evaluator;
	String identifier;
	Comparator<ComplexEvent> timeBasedComparator ;
	TimeModel tm;
	TimeStamp lastHearbitTimeStamp;
	GlobalState globalState;
	
	List<State> nextStates;
	
	// we cache event in same time epoch, in order to avoid ordering related problems
	List<Event> cachedEvents;
	
	private static AtomicInteger instanceCount=new AtomicInteger(0);
	
	public ConcurrentState(long duration, String predicate, List<EventClass> classes) {
		timeBasedComparator = ComplexEvent.getTimeBasedComparator();
		
		this.duration=duration;
		this.numClasses = classes.size();
		this.identifier = "Conc"+instanceCount.incrementAndGet();
		this.tm = Policies.getInstance().getTimeModel();
		this.lastHearbitTimeStamp = tm.getPointBasedTimeStamp(0);
		this.nextStates = new LinkedList<State>();
		this.cachedEvents = new LinkedList<Event>();
		this.globalState = GlobalState.getInstance();
		
		
		StringBuilder strbldr = new StringBuilder(classes.get(0).getName());
		for(int i=1;i<classes.size();i++) {
			strbldr.append("&");
			strbldr.append(classes.get(i).getName());
		}
		String classRepr = strbldr.toString();
		//create eventClass for complex events which will be generated
		EventType complexType = new ComplexEventType(classes);
		outputEventClass = new EventClass(classRepr, complexType);
		
		this.evaluator = JaninoEvalFactory.fromString(complexType, predicate);
		
		map=new HashMap<EventClass, List<ComplexEvent>>();
		for(EventClass ec : classes ) {
			List<ComplexEvent> queue = new LinkedList<ComplexEvent>();
			map.put(ec, queue);
		}
		
		for(EventClass ec: classes) {
			globalState.registerInputEventClassToState(ec, this);
		}
		globalState.registerOuputEventClassToState(outputEventClass, this);
	}
	
	public final EventClass getOutputEventClass() {
		return outputEventClass;
	}
	
	public void submitNext(Event e) {
		EventClass eClass = e.getEventClass();
		consumeHeartbit(e.getTimeStamp()); 	// Assuming events are submitted in total order
		List<ComplexEvent> toNextStateList = new LinkedList<ComplexEvent>();
		List<ComplexEvent> toBeAddedList = new LinkedList<ComplexEvent>();
		List<ComplexEvent> set = map.get(eClass);
		if(set==null)
			return ;
		
		//generate new partial matches
		for(Iterator<ComplexEvent> itr=set.iterator(); itr.hasNext();) {
			ComplexEvent partialMatch = itr.next();
			
			//check if the partial match is expired?
			int result = tm.getTimeStampComparator().compare(lastHearbitTimeStamp, partialMatch.getPermissibleTimeWindowTill());
			if(result>0) { //expired
				itr.remove();
				continue;
			} 
			
			long t1=System.nanoTime();
			ComplexEvent extendedPartialMatch = ComplexEvent.copyOf(partialMatch);
			int numSubEvents = extendedPartialMatch.addEvent(e);
			boolean constraintSatisfied = false;
			boolean moreAttribNeeded = false;
			try {
				constraintSatisfied = evaluator.evaluate(extendedPartialMatch);
			} catch(NullPointerException ex) {
				//ignore: signifies that not enough values are present to evaluate predicate to be true
				moreAttribNeeded = true;
			} catch(NoSuchFieldException nsfe) {
				throw new RuntimeException("Something wrong with predicate, possibly attribute names");
			}
			if(numSubEvents == numClasses) {
				if(constraintSatisfied && !extendedPartialMatch.isConsumed()) {
					// Generate all combinations for Complex Events like, E2;E4 and E4;E2 if E2 and E4 have same timestamp
					
					extendedPartialMatch.setEventClass(outputEventClass);
					toNextStateList.add(extendedPartialMatch);
					
					//extendedPartialMatch.setConsumed(true);
					//itr.remove();
				}
				//itr.remove();
			} else {
				/* 
				 * if(!moreAttribNeeded && !constraintSatisfied)
				 * 	discard the newEvent
				 * else
				 * 	add it to other queues
				 */
				if(moreAttribNeeded || constraintSatisfied ) 
					toBeAddedList.add(extendedPartialMatch);
			}
			long t2=System.nanoTime();
			System.err.println("Evaluating = "+(t2-t1)+" ns");
		}	
		
		// this new event will also start new partial match
		ComplexEvent newMatch = new ComplexEvent(outputEventClass);
		newMatch.addEvent(e);
		TimeStamp endts=Policies.getInstance().getTimeModel().getWindowCompletionTimeStamp(newMatch.getTimeStamp(),duration);
		newMatch.setPermissibleTimeWindowTill(endts);
		toBeAddedList.add(newMatch);
		
		long t1=System.nanoTime();
		for(ComplexEvent ce : toBeAddedList) {
			for(EventClass waitingFor : map.keySet() ) {
				if(!ce.containsEventOfClass(waitingFor.getName())) // ce already contains this
					map.get(waitingFor).add(ce);
				}
		}		
		
		for(Event generatedEvent:toNextStateList)
			GlobalState.getInstance().submitNext(generatedEvent);
		long t2=System.nanoTime();
		System.err.println("Propagting time = "+(t2-t1)+" ns");
	}
	
	// This is just a wrapper around sendHeartbit(long) for TimeStamp
	private void consumeHeartbit(TimeStamp ts) {
		if (ts instanceof IntervalTimeStamp) {
			IntervalTimeStamp its = (IntervalTimeStamp) ts;
			consumeHeartbit(its.getEndTime());
		}
		else
			assert false;
	}
	
	private void consumeHeartbit(long time) {
		TimeStamp newHeartbit = tm.getPointBasedTimeStamp(time);
		if(newHeartbit.compareTo(lastHearbitTimeStamp)!=0)
			cachedEvents.clear();
		lastHearbitTimeStamp = newHeartbit;
		//TODO clean unwanted instance?? or not?
	}

	@Override
	public void setPredicate(String predicate) {
		this.evaluator = JaninoEvalFactory.fromString(outputEventClass.getEventType(), predicate);
	}

	@Override
	public void propogatePartialMatches(
			Collection<ComplexEvent> newPartialMatches) {
		throw new UnsupportedOperationException();
		
	}

	@Override
	public void pumpHeartbeat(long heartbeat) {
		// it already does it
	}

}
