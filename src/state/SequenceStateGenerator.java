package state;

import java.util.LinkedList;
import java.util.List;

import evaluator.Evaluator;
import event.EventClass;
import event.eventtype.ComplexEventType;

public class SequenceStateGenerator {
	GlobalState globalState;
	
	
	public SequenceStateGenerator() {
		this.globalState = GlobalState.getInstance();
	}
	
	public State getBinarySequenceState(EventClass firstEventClass, EventClass secondEventClass, 
			Evaluator evaluator, long timeWindowDuration) {
		SequenceState firstState = null;
		SequenceState secondState = null;
		
		List<EventClass> list = new LinkedList<EventClass>();
		list.add(firstEventClass); 
		list.add(secondEventClass);
		ComplexEventType outputEventType = new ComplexEventType(list);
		
		String name = firstEventClass.getName()+";"+secondEventClass.getName();
		EventClass outputEventClass = new EventClass(name,outputEventType);
		
		// reuse
		if(globalState.getStateForOutputEventClass(name)!=null) {
			return globalState.getStateForOutputEventClass(name);
		}
		
		secondState = new SequenceState(outputEventClass,evaluator, timeWindowDuration,true);
		globalState.registerInputEventClassToState(secondEventClass, secondState);

		firstState = (SequenceState)globalState.getStateForOutputEventClass(firstEventClass.getName());
		if(firstState==null) {
			firstState = new SequenceState(firstEventClass,null,timeWindowDuration,false);
			globalState.registerInputEventClassToState(firstEventClass, firstState);
			globalState.registerOuputEventClassToState(firstEventClass, firstState);
			firstState.setFirstState(true);
		}
		firstState.addNextState(secondState);
		return secondState;
	} 
	
		
	
	public State getSequenceState(List<EventClass> eventClasses, Evaluator evaluator, long timeWindowDuration) {
		if(eventClasses.size()<=1)
			return null;
		
		if(eventClasses.size()==2) {
			return getBinarySequenceState(eventClasses.get(0), eventClasses.get(1), evaluator, timeWindowDuration);
		}
		
		EventClass first = eventClasses.get(0);
		List<EventClass> rest = eventClasses.subList(1, eventClasses.size());
		
		StringBuilder builder = new StringBuilder();
		for(EventClass ec:rest) {
			builder.append(ec.getName());
			builder.append(";");
		}
		builder.deleteCharAt(builder.length()-1);
		String restClassName = builder.toString();
		
		State restState = globalState.getStateForOutputEventClass(restClassName);
		if(restState==null) {
			restState = getSequenceState(rest, evaluator, timeWindowDuration);
		}
		return getBinarySequenceState(first, restState.getOutputEventClass(), evaluator, timeWindowDuration);
	}
	
}
