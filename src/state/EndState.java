package state;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import event.ComplexEvent;
import event.Event;
import event.EventClass;

/*
 * This is the accepting states of a query. It saves the generated events
 * until some other entity pulls events from it
 */

public class EndState implements State{
	List<Event> generatedEvents;
	
	public EndState() {
		generatedEvents = new LinkedList<Event>();
	}

	@Override
	public EventClass getOutputEventClass() {
		throw new RuntimeException("No event class for End State");
	}

	@Override
	public void submitNext(Event e) {
		generatedEvents.add(e);
	}
	
	public void getGeneratedEvents(Collection<Event> list) {
		list.addAll(generatedEvents);
		generatedEvents.clear();
	}

	@Override
	public void setPredicate(String predicate) {
		throw new RuntimeException("Can not put predicate on End State");
		
	}

	@Override
	public void propogatePartialMatches(
			Collection<ComplexEvent> newPartialMatches) {
		generatedEvents.addAll(newPartialMatches);
		
	}

	@Override
	public void pumpHeartbeat(long heartbeat) {
	}

}
