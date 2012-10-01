package state;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import evaluator.AlwaysTrueEvaluator;
import evaluator.Evaluator;
import evaluator.JaninoEvalFactory;
import event.ComplexEvent;
import event.Event;
import event.EventClass;
import event.PrimaryEvent;

public class UnionState implements State {
	EventClass outputEventClass;
	Evaluator evaluator;
	boolean isPredicateSet;
	String predicate;
	
	private static AtomicInteger instanceCount = new AtomicInteger();
	private String identifier;
	GlobalState globalState;
	
	public UnionState(EventClass outputEventClass) {
		this.outputEventClass = outputEventClass;
		this.identifier = "Union"+instanceCount.incrementAndGet();
		this.globalState = GlobalState.getInstance();
		this.isPredicateSet = false;
	}

	@Override
	public EventClass getOutputEventClass() {
		return outputEventClass;
	}

	@Override
	public void submitNext(Event e) {
		ComplexEvent ce;
		if(e instanceof PrimaryEvent) {
			ce = new ComplexEvent(outputEventClass);
			ce.addEvent(e);
		}
		else {
			ce = ComplexEvent.copyOf((ComplexEvent)e);
			ce.setEventClass(outputEventClass);
		}
		
		if(! isPredicateSet) {
			globalState.submitNext(ce);
			return;
		}
		
		boolean constraintSatisfied=false;
		
		try {
			constraintSatisfied = evaluator.evaluate(ce);
		}  catch(NoSuchFieldException nsfe) {
			throw new RuntimeException("Something wrong with predicate, possibly attribute names");
		}
		
		if(constraintSatisfied)
			globalState.submitNext(ce);
	}
	
	@Override
	public String toString() {
		return identifier+"["+outputEventClass.getName()+"]";
	}

	@Override
	public void setPredicate(String predicate) {
		this.evaluator = JaninoEvalFactory.fromString(outputEventClass.getEventType(), predicate);
		isPredicateSet = true;
		this.predicate = predicate;
	}

	@Override
	public void propogatePartialMatches(
			Collection<ComplexEvent> newPartialMatches) {
		for(ComplexEvent ce : newPartialMatches)
			submitNext(ce);
	}

	@Override
	public void pumpHeartbeat(long heartbeat) {
	}

}
