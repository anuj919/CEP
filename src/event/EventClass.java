package event;

import java.io.Serializable;

import event.eventtype.EventType;
import event.eventtype.PrimaryEventType;

@SuppressWarnings("serial")
public class EventClass implements Serializable, Comparable<EventClass> {
	String name;
	EventType eventType;
	
	private EventClass() {}
	
	public EventClass(String name, EventType eventType) {
		this.name=name;
		this.eventType=eventType;
	}

	public String getName() {
		return name;
	}

	public EventType getEventType() {
		return eventType;
	}
	
	public boolean equals(Object o) {
		if(o == this) return true;
		return (o!=null && o instanceof EventClass 
				&& getName().equals(((EventClass)o).getName()) );
				//&& ((EventClass)o).getEventType().equals(getEventType())); event class should have unique names => equal name -> equal type
	}
	
	public int hashCode() {
		return name.hashCode();
	}
	
	protected AttributeType getAttributeType(String attr) {
		return getEventType().getAttributeType(attr);
	}
	
	public boolean isComplexEventClass() {
		if(eventType instanceof PrimaryEventType)
			return false;
		else
			return true;
	}
	
	@Override
	public String toString() {
		return name+"["+eventType+"]";
	}
	
	@Override
	public int compareTo(EventClass other) {
		if(other == null)
			throw new NullPointerException();
		return name.compareTo(other.name);
	}
}
