package state;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

import datastructures.MultiQueue;
/* This class implements OneState approach for Conjunction Query.
 */

public class ConcurrentState implements State {
	
	static int NUM_PROC = Runtime.getRuntime().availableProcessors();
	static ExecutorService threadpool = Executors.newFixedThreadPool(NUM_PROC);
	
	Map<EventClass, MultiQueue<ComplexEvent> > map;
	EventClass outputEventClass;
	long duration;
	int numClasses;
	//Evaluator evaluator;
	Map<Set<String>,Evaluator> evaluators;
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
		
		//this.evaluator = JaninoEvalFactory.fromString(complexType, predicate);
		this.setPredicate(predicate);
		
		map=new HashMap<EventClass, MultiQueue<ComplexEvent>>();
		for(EventClass ec : classes ) {
			MultiQueue<ComplexEvent> multiQueue = new MultiQueue<ComplexEvent>(NUM_PROC);
			map.put(ec, multiQueue);
		}
		
		for(EventClass ec: classes) {
			globalState.registerInputEventClassToState(ec, this);
		}
		globalState.registerOuputEventClassToState(outputEventClass, this);
	}
	
	public final EventClass getOutputEventClass() {
		return outputEventClass;
	}
	
	public void submitNext(final Event e) {
		EventClass eClass = e.getEventClass();
		consumeHeartbit(e.getTimeStamp()); 	// Assuming events are submitted in total order
		//final Collection<ComplexEvent> toNextStateList = new ConcurrentLinkedQueue<ComplexEvent>();
		//final Collection<ComplexEvent> toBeAddedList = new ConcurrentLinkedQueue<ComplexEvent>();
		MultiQueue<ComplexEvent> multiQueue = map.get(eClass);
		if(multiQueue==null)
			return ;
		
		final Collection<Callable<Object>> tasks = new ArrayList<Callable<Object>>();
		
		//generate new partial matches
		for(int i=0;i<multiQueue.getNumInternalQueue();i++) {
			final Queue<ComplexEvent> list = multiQueue.getList(i);
			tasks.add(Executors.callable( new Runnable() {
				@Override
				public void run() {
					for(Iterator<ComplexEvent> itr=list.iterator(); itr.hasNext();) {
						ComplexEvent partialMatch = itr.next();
						
						//check if the partial match is expired?
						int result = tm.getTimeStampComparator().compare(lastHearbitTimeStamp, partialMatch.getPermissibleTimeWindowTill());
						if(result>0) { //expired
							itr.remove();
							continue;
						} 
						
						ComplexEvent extendedPartialMatch = ComplexEvent.copyOf(partialMatch);
						int numSubEvents = extendedPartialMatch.addEvent(e);
						boolean constraintSatisfied = false;
						boolean moreAttribNeeded = false;
						try {
							//constraintSatisfied = evaluator.evaluate(extendedPartialMatch);
							Set<String> eventClassesAlreadyPresent = extendedPartialMatch.getEventClassesAlreadyPresent();
							Evaluator evaluator = evaluators.get(eventClassesAlreadyPresent);
							if(evaluator == null)
								constraintSatisfied=true;
							else
								constraintSatisfied = evaluator.evaluate(extendedPartialMatch);
						//} catch(NullPointerException ex) {
						//	//ignore: signifies that not enough values are present to evaluate predicate to be true
						//	moreAttribNeeded = true;
						} catch(NoSuchFieldException nsfe) {
							throw new RuntimeException("Something wrong with predicate, possibly attribute names");
						}
						if(numSubEvents == numClasses) {
							if(constraintSatisfied && !extendedPartialMatch.isConsumed()) {
								extendedPartialMatch.setEventClass(outputEventClass);
								//toNextStateList.add(extendedPartialMatch);
								GlobalState.getInstance().submitNext(extendedPartialMatch);
								//extendedPartialMatch.setConsumed(true);
								//itr.remove();
							}
						} else {
							/* 
							 * if(!moreAttribNeeded && !constraintSatisfied)
							 * 	discard the newEvent
							 * else
							 * 	add it to other queues
							 */
							if(moreAttribNeeded || constraintSatisfied ) {
								// toBeAddedList.add(extendedPartialMatch);
								for(EventClass waitingFor : map.keySet() ) {
									if(!extendedPartialMatch.containsEventOfClass(waitingFor.getName())) // ce already contains this
										map.get(waitingFor).add(extendedPartialMatch);
								}
							}
								
						}
					}	
					
				}
			} ));	
		}
		
		try {
	        threadpool.invokeAll(tasks);
	    } catch (InterruptedException ex) {
	    	ex.printStackTrace();
	    }
		
		// this new event will also start new partial match
		ComplexEvent newMatch = new ComplexEvent(outputEventClass);
		newMatch.addEvent(e);
		TimeStamp endts=Policies.getInstance().getTimeModel().getWindowCompletionTimeStamp(newMatch.getTimeStamp(),duration);
		newMatch.setPermissibleTimeWindowTill(endts);
		//toBeAddedList.add(newMatch);
		
		long t1=System.nanoTime();
		for(EventClass waitingFor : map.keySet() ) {
			if(!newMatch.containsEventOfClass(waitingFor.getName())) // ce already contains this
				map.get(waitingFor).add(newMatch);
		}		
		
		//for(Event generatedEvent:toNextStateList)
		//	GlobalState.getInstance().submitNext(generatedEvent);
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
	
	private void consumeHeartbit(double time) {
		TimeStamp newHeartbit = tm.getPointBasedTimeStamp(time);
		if(newHeartbit.compareTo(lastHearbitTimeStamp)!=0)
			cachedEvents.clear();
		lastHearbitTimeStamp = newHeartbit;
		//TODO clean unwanted instance?? or not?
	}

	@Override
	public void setPredicate(String predicate) {
		//this.evaluator = JaninoEvalFactory.fromString(outputEventClass.getEventType(), predicate);
		this.evaluators = new HashMap<Set<String>, Evaluator>();
		String[] slicedPredicates = predicate.split("&&");		
		Map<Set<String>,Set<String>> eventClassesToPredicate = new HashMap<Set<String>, Set<String>>();
		// build a map for each event-class and corresponding predicates
		for(String pred : slicedPredicates) {
			Set<String> eventClasses = getEventClassesInPredicate(pred);
			Set<String> correspondingPredicates = eventClassesToPredicate.get(eventClasses);
			if(correspondingPredicates==null) {
				correspondingPredicates=new HashSet<String>();
				eventClassesToPredicate.put(eventClasses, correspondingPredicates);
			}
			correspondingPredicates.add(pred);
		}
		
		for(Set<String> ec : eventClassesToPredicate.keySet()) {
			StringBuilder conjunctionPred = new StringBuilder();
			for(String pred : eventClassesToPredicate.get(ec)) {
				conjunctionPred.append(" && ");
				conjunctionPred.append(pred);
			}
			// remove the first &&
			conjunctionPred.delete(0, " && ".length());
			Evaluator evaluator = JaninoEvalFactory.fromString(outputEventClass.getEventType(), conjunctionPred.toString()); 
			evaluators.put(ec, evaluator);
		}
	}

	@Override
	public void propogatePartialMatches(
			Collection<ComplexEvent> newPartialMatches) {
		throw new UnsupportedOperationException();
		
	}

	@Override
	public void pumpHeartbeat(double heartbeat) {
		// it already does it
	}
	
	private Set<String> getEventClassesInPredicate(String predicate) {
		Set<String> returnSet = new HashSet<String>();
		Pattern pattern = Pattern.compile("[a-zA-Z][a-zA-Z0-9.]*", Pattern.DOTALL);
		Matcher matcher = pattern.matcher(predicate);
		while(matcher.find()){
		    String eClassWithAttr = matcher.group(); // this will include the $
		    String eClass = eClassWithAttr.split("\\.")[0];
		    returnSet.add(eClass);
		}
		return returnSet;
	}

}
