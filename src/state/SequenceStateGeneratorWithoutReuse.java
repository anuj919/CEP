package state;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import evaluator.Evaluator;
import event.EventClass;
import event.eventtype.ComplexEventType;

/* 
 * This class generates right deep tree of sequence automaton from given 
 * event-classes. It does NOT reuse already existing suffices.
 */

public class SequenceStateGeneratorWithoutReuse {
	GlobalState globalState;
	
	
	public SequenceStateGeneratorWithoutReuse() {
		this.globalState = GlobalState.getInstance();
	}
	
	public Map<String,SequenceState> getBinarySequenceState(EventClass firstEventClass, EventClass secondEventClass, 
			Evaluator evaluator, long timeWindowDuration) {
		List<EventClass> list = new LinkedList<EventClass>();
		list.add(firstEventClass); list.add(secondEventClass);
		return getBinarySequenceState(list, evaluator, timeWindowDuration,true,firstEventClass.getName());		
	}
	
	private Map<String,SequenceState> getBinarySequenceState(List<EventClass> eventClasses, 
			Evaluator evaluator, long timeWindowDuration, boolean first, String prefixClassesStr) {
		SequenceState firstState = null;
		SequenceState secondState = null;
		Map<String,SequenceState> map = new HashMap<String, SequenceState>();
		
		List<EventClass> prefixEventClasses = eventClasses.subList(0, eventClasses.size()-1);
		EventClass secondEventClass = eventClasses.get(eventClasses.size()-1);
		EventClass firstEventClass = new EventClass(prefixClassesStr, new ComplexEventType(prefixEventClasses));
		EventClass actualFirstEventClass = eventClasses.get(eventClasses.size()-2);
		
		
		ComplexEventType outputEventType = new ComplexEventType(eventClasses);
		String name = firstEventClass.getName()+";"+secondEventClass.getName();
		EventClass outputEventClass = new EventClass(name, outputEventType);
			
		secondState = new SequenceState(outputEventClass,evaluator, timeWindowDuration,false);
		if(!secondEventClass.isComplexEventClass())  // register only for primary events
			globalState.registerInputEventClassToState(secondEventClass, secondState);

		firstState = new SequenceState(firstEventClass,null,timeWindowDuration,false);
		if(!actualFirstEventClass.isComplexEventClass())	// register only for primary events
			globalState.registerInputEventClassToState(actualFirstEventClass, firstState);
		firstState.addNextState(secondState);
		if(first)
			firstState.setFirstState(true);
		map.put("first", firstState);
		map.put("last", secondState);
		return map;
	}
	
	private Map<String,SequenceState> getSequenceState(List<EventClass> eventClasses, int currentClass, Evaluator evaluator, long timeWindowDuration,String prefixClassesStr) {
		if(eventClasses.size()==currentClass+1)
			return null;
		
		EventClass firstEventClass = eventClasses.get(currentClass);
		List<EventClass> newPrefixClasses = eventClasses.subList(0, currentClass+1);
		String newPrefixClassesStr = (prefixClassesStr.equals("")? ("") : (prefixClassesStr+";"))+firstEventClass.getName();
		
		if(eventClasses.size()==currentClass+2) {
			Map<String,SequenceState> map = getBinarySequenceState(eventClasses, evaluator, timeWindowDuration,currentClass==0, newPrefixClassesStr);
			map.get("first").setDoNotPublishResult(true);
			//globalState.registerOuputEventClassToState(s.getOutputEventClass(), s);
			return map;
		}

		Map<String,SequenceState> restmap = getSequenceState(eventClasses, currentClass+1, evaluator, timeWindowDuration,newPrefixClassesStr);
		Map<String,SequenceState> returnMap = new HashMap<String, SequenceState>();
		
		EventClass newPrefixOutputClass = new EventClass(newPrefixClassesStr,new ComplexEventType(newPrefixClasses));
		
		SequenceState firstState = new SequenceState(newPrefixOutputClass,null,timeWindowDuration,false);
		globalState.registerInputEventClassToState(firstEventClass, firstState);
		firstState.addNextState(restmap.get("first"));
		if(currentClass==0)
			firstState.setFirstState(true);
		else
			firstState.setEvaluator(evaluator);
		returnMap.put("first", firstState);
		returnMap.put("last", restmap.get("last"));
		return returnMap;
	}
	
	public Map<String,SequenceState> getSequenceState(List<EventClass> eventClasses, Evaluator evaluator, long timeWindowDuration) {
		return getSequenceState(eventClasses, 0, evaluator, timeWindowDuration, "");
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
