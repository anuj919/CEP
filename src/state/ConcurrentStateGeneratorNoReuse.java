package state;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Collections2;

import evaluator.Evaluator;
import event.EventClass;
import event.eventtype.ComplexEventType;
import event.eventtype.EventType;

/* This class generates set of states to implement conjunctive query
 * using ConcurrentStateWithNoReuse approach.
 */

public class ConcurrentStateGeneratorNoReuse {
	GlobalState globalState;	
	SequenceStateGeneratorWithoutReuse seqGenerator;
	
	public ConcurrentStateGeneratorNoReuse() {
		globalState = GlobalState.getInstance();
		this.seqGenerator=new SequenceStateGeneratorWithoutReuse();
	}
	
	public State generateConcurrentState(List<EventClass> _eventClasses, Evaluator evaluator, long timeDuration) {
		List<EventClass> eventClasses = new ArrayList<EventClass>(_eventClasses);
				
		// Generating classname for this
		StringBuilder buffer=new StringBuilder();
				
		Iterator<EventClass> itr = eventClasses.iterator();  
		buffer.append(itr.next().getName());
		buffer.append("&");
		for(;itr.hasNext();) {
			buffer.append(itr.next().getName());
			buffer.append("&");
		}
		buffer.deleteCharAt(buffer.length()-1);
		String strRep = buffer.toString();
		
		State combinePermuatationState = globalState.getStateForOutputEventClass(strRep);
		
		if(combinePermuatationState == null ) {
			EventType resultType = new ComplexEventType(eventClasses);
			EventClass resultClass = new EventClass(strRep,resultType);
			combinePermuatationState = new UnionState(resultClass);
			globalState.registerOuputEventClassToState(resultClass, combinePermuatationState);

		
			if(eventClasses.size()==1) {
				globalState.registerInputEventClassesToState(eventClasses,combinePermuatationState);
			} else {
				Collection<List<EventClass>> allPermutations = Collections2.permutations(eventClasses);
				for(List<EventClass> subset : allPermutations) {
					combineState(subset, combinePermuatationState, timeDuration,evaluator);
				}
			}
		}
		
		return combinePermuatationState;
	}
	

	private void combineState(List<EventClass> curPermEventClasses,State lastState, long duration, Evaluator evaluator) {
		if(curPermEventClasses.size()<=1) 
			return;
		
		StringBuilder buffer=new StringBuilder();
				
		Iterator<EventClass> itr = curPermEventClasses.iterator();  
		for(;itr.hasNext();) {
			buffer.append(itr.next().getName());
			buffer.append(";");
		}
		buffer.deleteCharAt(buffer.length()-1);
		
		String newEventClassName = buffer.toString();
		EventClass eClass = globalState.getEventClass(newEventClassName);
		if(eClass == null) {
			EventType type = new ComplexEventType(curPermEventClasses);
			eClass = new EventClass(newEventClassName, type);
			globalState.registerEventClass(eClass);
		}

		// if it is already present, reuse it
		if(!shouldGenerateStateForEventClass(newEventClassName)) {
			globalState.registerInputEventClassToState(eClass, lastState);
			return;
		}
		SequenceState lastSeqState = seqGenerator.getSequenceState(curPermEventClasses, evaluator, duration).get("last");
		lastSeqState.setDoNotPublishResult(true);
		lastSeqState.addNextState(lastState);		
	}
	
	boolean shouldGenerateStateForEventClass(String ecName) {
		EventClass ec = globalState.getEventClass(ecName);
		if(ec==null)
			return true;
		
		if(ec.isComplexEventClass()) {
			State s = globalState.getStateForOutputEventClass(ec.getName());
			if(s==null)
				return true;
		}
		return false;
	}
	
	
}
