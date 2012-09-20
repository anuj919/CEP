package state;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

import datastructures.SortedTreeList;

import time.timemodel.TimeModel;
import time.timestamp.IntervalTimeStamp;
import time.timestamp.TimeStamp;

import evaluator.Evaluator;
import evaluator.JaninoEvalFactory;
import event.ComplexEvent;
import event.Event;
import event.EventClass;
import event.eventtype.ComplexEventType;
import event.util.Policies;

public class SequenceState implements State {
	long timeWindowDuration;
	EventClass outputEventClass;
	long duration;
	List<State> nextStates;
	boolean firstState;
	
	// we cache event in same time epoch, in order to avoid ordering related problems
	List<Event> cachedEvents;
	GlobalState globalState;
	
	
	SortedTreeList<ComplexEvent> partialMatches;
	Evaluator evaluator;
	Comparator<ComplexEvent> timeBasedComparator ;
	TimeStamp lastHearbitTimeStamp;
	private static AtomicInteger instanceCount = new AtomicInteger();
	private String identifier;
	private TimeModel tm;
	private boolean publishResult;
	
	public SequenceState(EventClass outputEventClass, Evaluator evaluator, long timeWindowDuration, boolean publishResult) {
		this.timeWindowDuration = timeWindowDuration;
		this.outputEventClass = outputEventClass;
		this.duration = timeWindowDuration;
		this.nextStates = new LinkedList<State>();
		this.cachedEvents = new LinkedList<Event>();
		
		
		this.timeBasedComparator = ComplexEvent.getTimeBasedComparator();
		this.tm = Policies.getInstance().getTimeModel();
		this.lastHearbitTimeStamp = tm.getPointBasedTimeStamp(0);
		this.identifier = "Seq"+instanceCount.incrementAndGet();
		this.partialMatches = new SortedTreeList<ComplexEvent>(timeBasedComparator);
		this.evaluator = evaluator;
		this.firstState = false;
		this.publishResult = publishResult;
		
		globalState = GlobalState.getInstance();
		if(publishResult)
			globalState.registerOuputEventClassToState(outputEventClass, this);
	}
	
	public void setFirstState(boolean first) {
		this.firstState=first;
	}


	public void addNextState(State s) {
		nextStates.add(s);
	}
	
	public void addNextStates(Collection<State> s) {
		nextStates.addAll(s);
	}

	@Override
	public EventClass getOutputEventClass() {
		return outputEventClass;
	}
	
	// This is just a wrapper around sendHeartbit(long) for TimeStamp
	private void sendHeartbit(TimeStamp ts) {
		if (ts instanceof IntervalTimeStamp) {
			IntervalTimeStamp its = (IntervalTimeStamp) ts;
			sendHeartbit(its.getEndTime());
		}
		else
			assert false;
	}

	@Override
	public void sendHeartbit(long time) {
		TimeStamp newHeartbit = tm.getPointBasedTimeStamp(time);
		if(newHeartbit.compareTo(lastHearbitTimeStamp)!=0)
			cachedEvents.clear();
		lastHearbitTimeStamp = newHeartbit;
	}

	@Override
	public void submitNext(Event e) {
		EventClass eClass = e.getEventClass();
		sendHeartbit(e.getTimeStamp());		// Assuming events are submitted in total order
		List<ComplexEvent> toNextStateList = new LinkedList<ComplexEvent>();
		cachedEvents.add(e);
		
		if(firstState) {
			ComplexEvent ce = new ComplexEvent(outputEventClass);
			ce.addEvent(e);
			TimeStamp endts=Policies.getInstance().getTimeModel().getWindowCompletionTimeStamp(ce.getTimeStamp(),duration);
			ce.setPermissibleTimeWindowTill(endts);
			toNextStateList.add(ce);
		}
		
		//generate new partial matches
		for(Iterator<ComplexEvent> itr=partialMatches.iterator(); itr.hasNext();) {
			ComplexEvent partialMatch = itr.next();
			
			//check if the partial match is expired?
			int result = tm.getTimeStampComparator().compare(lastHearbitTimeStamp, partialMatch.getPermissibleTimeWindowTill());
			if(result>0) { //expired
				itr.remove();
				continue;
			}
			
			// check if new event can be next event of partialMatch?
			if(!tm.canBeNext(e.getTimeStamp(), partialMatch.getTimeStamp()))
				continue;
			
			ComplexEvent extendedPartialMatch = ComplexEvent.copyOf(partialMatch);
			extendedPartialMatch.addEvent(e);
			boolean constraintSatisfied = false;
			try {
				constraintSatisfied = evaluator.evaluate(extendedPartialMatch);
			} catch(NullPointerException ex) {
				//signifies that not enough values are present to evaluate predicate to be true
				// this means that constraint was not violated thus we propogate the partialMatch
				constraintSatisfied = true;
			} catch(NoSuchFieldException nsfe) {
				throw new RuntimeException("Something wrong with predicate, possibly attribute names");
			}
			
			if(constraintSatisfied && !extendedPartialMatch.isConsumed()) {
				extendedPartialMatch.setEventClass(outputEventClass);
				toNextStateList.add(extendedPartialMatch);
				//extendedPartialMatch.setConsumed(true);
				//itr.remove();
			}
		}
		
		if(toNextStateList.size()>0)
			for(State state: nextStates)
				state.propogatePartialMatches(toNextStateList);
		if(!firstState && publishResult )
			for(Event generated : toNextStateList)
				globalState.submitNext(generated);
		
	}


	@Override
	public void propogatePartialMatches(Collection<ComplexEvent> newPartialMatches) {
		List<ComplexEvent> toNextStateList= new LinkedList<ComplexEvent>();
		for(ComplexEvent ce : newPartialMatches)
			sendHeartbit(ce.getTimeStamp());
	
		// TODO Check if it can match with existing cached events, also add to partial matches
		
		
		for(Event cachedEvent : cachedEvents) {
			// try to add this cachedEvent to newPartialMatches
			for(ComplexEvent partialMatch : newPartialMatches ) {						
				ComplexEvent extendedPartialMatch = ComplexEvent.copyOf(partialMatch);
				extendedPartialMatch.addEvent(cachedEvent);
				boolean constraintSatisfied = false;
				try {
					constraintSatisfied = evaluator.evaluate(extendedPartialMatch);
				} catch(NullPointerException ex) {
					//signifies that not enough values are present to evaluate predicate to be true
					// this means that constraint was not violated thus we propogate the partialMatch
					constraintSatisfied = true;
				} catch(NoSuchFieldException nsfe) {
					throw new RuntimeException("Something wrong with predicate, possibly attribute names");
				}
			
				if(constraintSatisfied && !extendedPartialMatch.isConsumed()) {
					extendedPartialMatch.setEventClass(outputEventClass);
					toNextStateList.add(extendedPartialMatch);
					//extendedPartialMatch.setConsumed(true);
					//itr.remove();
				}
			}
		}
		
		partialMatches.addAll(newPartialMatches);
		//TODO Do NOT update lastHeartbit timestamp here
		if(toNextStateList.size()>0) {
			for(State state: nextStates)
				state.propogatePartialMatches(toNextStateList);
			if(publishResult)
				for(Event e : toNextStateList)
					globalState.submitNext(e);
		}
		
	}
	
	@Override
	public String toString() {
		return identifier+"["+outputEventClass.getName()+"]";
	}


	@Override
	public void setPredicate(String predicate) {
		this.evaluator = JaninoEvalFactory.fromString(outputEventClass.getEventType(), predicate);
	}
	
	public void setDoNotPublishResult(boolean b) {
		this.publishResult = ! b;
	}

	public void setEvaluator(Evaluator evaluator) {
		this.evaluator = evaluator;
	}
}
