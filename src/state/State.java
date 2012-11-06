package state;

import java.util.Collection;

import event.ComplexEvent;
import event.Event;
import event.EventClass;

/*
 * Abstract class representing any automaton state
 */

public interface State {
	public void submitNext(Event e);
	public EventClass getOutputEventClass();
	public void setPredicate(String predicate);
	public void propogatePartialMatches(Collection<ComplexEvent> newPartialMatches);
	public void pumpHeartbeat(long heartbeat);
}
