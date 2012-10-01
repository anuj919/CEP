package state;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import time.timestamp.IntervalTimeStamp;
import time.timestamp.TimeStamp;

import event.Event;
import event.EventClass;

public class GlobalState {
	Map<String, EventClass> knownEventClasses;
	Map<String, List<State>> inputEventToStates;
	Map<String, State> generatedEventClassToState;
	List<State> allStates;
	
	private static GlobalState instance;
	
	private static List<State> emptyList = new LinkedList<State>();
	
	private GlobalState() {
		knownEventClasses = new HashMap<String, EventClass>();
		inputEventToStates = new HashMap<String, List<State>>();
		generatedEventClassToState = new HashMap<String, State>();
		allStates=new LinkedList<State>();
	}
	
	public static GlobalState getInstance() {
		if(instance==null)
			instance = new GlobalState();
		return instance;
	}
	
	public boolean registerEventClass(EventClass eventClass) {
		if(knownEventClasses.containsKey(eventClass.getName()))
			return false;
		knownEventClasses.put(eventClass.getName(), eventClass);
		return true;
	}
	
	public  EventClass getEventClass(String className ){
		return knownEventClasses.get(className);
	}
	
	public boolean registerOuputEventClassToState(EventClass ec, State s) {
		if(generatedEventClassToState.containsKey(ec.getName()))
			return false;
		
		if(!knownEventClasses.containsKey(ec.getName()))
			knownEventClasses.put(ec.getName(),ec);
		generatedEventClassToState.put(ec.getName(), s);
		return true;
	}
	
	public State getStateForOutputEventClass(String name)  {
		return generatedEventClassToState.get(name);
	}
	
	public void registerInputEventClassToState(EventClass ec, State s) {
		assert knownEventClasses.containsKey(ec);
		List<State> affectedStates= inputEventToStates.get(ec.getName());
		if(affectedStates == null) {
			affectedStates = new LinkedList<State>();
			inputEventToStates.put(ec.getName(), affectedStates);
		}
		affectedStates.add(s);
	}
	
	public void registerInputEventClassesToState(List<EventClass> eclist, State s) {
		for(EventClass ec:eclist)
			registerInputEventClassToState(ec, s);
	}
	
	public List<State> getStatesForInputEventClass(EventClass ec) {
		List<State> states = inputEventToStates.get(ec.getName());
		if(states==null)
			return emptyList;
		else
			return states;
	}
	
	public void submitNext(Event e) {
		for(State s:getStatesForInputEventClass(e.getEventClass())) {
			s.submitNext(e);
		}
	}

	public void submitHeartbeat(TimeStamp timeStamp) {
		long heartbeat=0;
		if(timeStamp instanceof IntervalTimeStamp)
			heartbeat=((IntervalTimeStamp)timeStamp).getEndTime();
		else
			throw new RuntimeException("Unknown timestamp type");
		for(State s:allStates) {
			s.pumpHeartbeat(heartbeat);
		}
	}

	public void registerStateForHeartBeat(State s) {
		allStates.add(s);
	}
	
}
