package state;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.paukov.combinatorics.Factory;
import org.paukov.combinatorics.Generator;
import org.paukov.combinatorics.ICombinatoricsVector;

import com.google.common.collect.Collections2;

import evaluator.Evaluator;
import evaluator.JaninoEvalFactory;
import event.EventClass;
import event.eventtype.ComplexEventType;
import event.eventtype.EventType;

/* This class generates set of states to implement conjunctive query
 * using ConcurrentStateWithNoReuse approach.
 */

public class ConcurrentStateGeneratorNoReuse {
	GlobalState globalState;	
	SequenceStateGeneratorWithoutReuse seqGenerator;
	EventClass outputEventClass;
	Map<Integer, Evaluator> evaluators;
	
	public ConcurrentStateGeneratorNoReuse() {
		globalState = GlobalState.getInstance();
		this.seqGenerator=new SequenceStateGeneratorWithoutReuse();
	}
	
	public State generateConcurrentState(List<EventClass> _eventClasses, String predicate, long timeDuration) {
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
			outputEventClass = new EventClass(strRep,resultType);
			combinePermuatationState = new UnionState(outputEventClass);
			globalState.registerOuputEventClassToState(outputEventClass, combinePermuatationState);

			evaluators = getEvaluators(predicate,eventClasses);
			
			if(eventClasses.size()==1) {
				globalState.registerInputEventClassesToState(eventClasses,combinePermuatationState);
			} else {
				Collection<List<EventClass>> allPermutations = Collections2.permutations(eventClasses);
				for(List<EventClass> subset : allPermutations) {
					combineState(subset, combinePermuatationState, timeDuration,evaluators);
				}
			}
		}
		
		return combinePermuatationState;
	}
	

	private void combineState(List<EventClass> curPermEventClasses,State lastState, long duration, Map<Integer,Evaluator> evaluators) {
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
		SequenceState lastSeqState = seqGenerator.getSequenceState(curPermEventClasses, evaluators, duration).get("last");
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
	
	private Map<Integer, Evaluator> getEvaluators(String predicate, List<EventClass> allEventClasses) {
		Map<Integer, Evaluator> evaluators = new HashMap<Integer, Evaluator>();
		
		
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
		
		Map<Integer,Set<String>> eventClassToPredicates_includingSubsets = new HashMap<Integer, Set<String>>();
		ICombinatoricsVector<EventClass> initialVector = Factory.createVector(allEventClasses);
		Generator<EventClass> gen = Factory.createSubSetGenerator(initialVector);
		//PermutationsOfN<EventClass> permuatationsOfN = new PermutationsOfN<EventClass>();
		for(ICombinatoricsVector<EventClass> subset : gen) {
			if(subset.getSize()==0)
				continue;
			
			Set<String> predicates = new HashSet<String>();
			Set<String> eventClassNames = new HashSet<String>();
			int hash = 0;
			for (EventClass eClass : subset) {
				hash^= eClass.getName().hashCode();
				eventClassNames.add(eClass.getName());
			}
			
			if(eventClassesToPredicate.containsKey(eventClassNames))
				predicates.addAll(eventClassesToPredicate.get(eventClassNames));
			if(subset.getSize()>1) { 
				// get all subset of length k-1, add all their predicates
				for(int i=0;i<subset.getSize();i++) {
					// get set containing all class except i
					int pastHash = hash ^ subset.getValue(i).getName().hashCode();
					// add all predicates corresponding to set
					if(eventClassToPredicates_includingSubsets.containsKey(pastHash))
					predicates.addAll(eventClassToPredicates_includingSubsets.get(pastHash));
				}
			}
			
			if(predicates.size()==0)
				continue;
			
			StringBuilder predicateForSubset = new StringBuilder();
			for(String pred : predicates) {
				predicateForSubset.append(" && ");
				predicateForSubset.append(pred);
			}
			predicateForSubset.delete(0, " && ".length());
			
			eventClassToPredicates_includingSubsets.put(hash, predicates);
			evaluators.put(hash, JaninoEvalFactory.fromString(outputEventClass.getEventType(), predicateForSubset.toString()));
		}
		return evaluators;
		
	}
	
	
}
