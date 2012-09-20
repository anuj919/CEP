package state;

import java.util.Collection;
import java.util.List;

import event.ComplexEvent;
import event.Event;
import event.EventClass;

public interface State {
	public void submitNext(Event e);
	public EventClass getOutputEventClass();
	public void sendHeartbit(long time);
	public void setPredicate(String predicate);
	public void propogatePartialMatches(Collection<ComplexEvent> newPartialMatches);
}
