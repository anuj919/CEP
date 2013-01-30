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
	
	/*public Map<String,SequenceState> getBinarySequenceState(EventClass firstEventClass, EventClass secondEventClass, 
			Evaluator evaluator, long timeWindowDuration) {
		List<EventClass> list = new LinkedList<EventClass>();
		list.add(firstEventClass); list.add(secondEventClass);
		
		Map<Integer,Evaluator> evaluators = new HashMap<Integer, Evaluator>();
		evaluators.put(firstEventClass.getName().hashCode(), )
		
		return getBinarySequenceState(list, evaluator, timeWindowDuration,true,firstEventClass.getName());		
	}*/
	
	private Map<String,SequenceState> getBinarySequenceState(List<EventClass> eventClasses, 
			Map<Integer,Evaluator> evaluators, long timeWindowDuration, boolean first, String prefixClassesStr, int prefixClassesHash) {
		SequenceState firstState = null;
		SequenceState secondState = null;
		Map<String,SequenceState> map = new HashMap<String, SequenceState>();
		
		List<EventClass> prefixEventClasses = eventClasses.subList(0, eventClasses.size()-1);
		EventClass secondEventClass = eventClasses.get(eventClasses.size()-1);
		EventClass firstStateOutputEventClass = new EventClass(prefixClassesStr, new ComplexEventType(prefixEventClasses));
		EventClass actualFirstEventClass = eventClasses.get(eventClasses.size()-2);
		
		
		ComplexEventType outputEventType = new ComplexEventType(eventClasses);
		String name = firstStateOutputEventClass.getName()+";"+secondEventClass.getName();
		EventClass outputEventClass = new EventClass(name, outputEventType);
		
		int finalPrefixClassesHash = prefixClassesHash ^ secondEventClass.getName().hashCode();
			
		secondState = new SequenceState(outputEventClass,evaluators.get(finalPrefixClassesHash), timeWindowDuration,false);
		if(!secondEventClass.isComplexEventClass())  // register only for primary events
			globalState.registerInputEventClassToState(secondEventClass, secondState);

		firstState = new SequenceState(firstStateOutputEventClass,evaluators.get(prefixClassesHash),timeWindowDuration,false);
		if(!actualFirstEventClass.isComplexEventClass())	// register only for primary events
			globalState.registerInputEventClassToState(actualFirstEventClass, firstState);
		firstState.addNextState(secondState);
		if(first)
			firstState.setFirstState(true);
		map.put("first", firstState);
		map.put("last", secondState);
		return map;
	}
	
	private Map<String,SequenceState> getSequenceState(List<EventClass> eventClasses, int currentClass, Map<Integer,Evaluator> evaluators, long timeWindowDuration,String prefixClassesStr, int prefixClassesHash) {
		if(eventClasses.size()==currentClass+1)
			return null;
		
		EventClass firstEventClass = eventClasses.get(currentClass);
		List<EventClass> newPrefixClasses = eventClasses.subList(0, currentClass+1);
		String newPrefixClassesStr = (prefixClassesStr.equals("")? ("") : (prefixClassesStr+";"))+firstEventClass.getName();
		int newPrefixClassesHash = prefixClassesHash ^ firstEventClass.getName().hashCode();
		
		if(eventClasses.size()==currentClass+2) {
			Map<String,SequenceState> map = getBinarySequenceState(eventClasses, evaluators, timeWindowDuration,currentClass==0, newPrefixClassesStr, newPrefixClassesHash);
			map.get("first").setDoNotPublishResult(true);
			//globalState.registerOuputEventClassToState(s.getOutputEventClass(), s);
			return map;
		}

		Map<String,SequenceState> restmap = getSequenceState(eventClasses, currentClass+1, evaluators, timeWindowDuration,newPrefixClassesStr, newPrefixClassesHash);
		Map<String,SequenceState> returnMap = new HashMap<String, SequenceState>();
		
		EventClass newPrefixOutputClass = new EventClass(newPrefixClassesStr,new ComplexEventType(newPrefixClasses));
		
		SequenceState firstState = new SequenceState(newPrefixOutputClass,null,timeWindowDuration,false);
		globalState.registerInputEventClassToState(firstEventClass, firstState);
		firstState.addNextState(restmap.get("first"));
		if(currentClass==0)
			firstState.setFirstState(true);
		else
			firstState.setEvaluator(evaluators.get(newPrefixClassesHash));
		returnMap.put("first", firstState);
		returnMap.put("last", restmap.get("last"));
		return returnMap;
	}
	
	public Map<String,SequenceState> getSequenceState(List<EventClass> eventClasses, Map<Integer,Evaluator> evaluators, long timeWindowDuration) {
		return getSequenceState(eventClasses, 0, evaluators, timeWindowDuration, "",0);
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
