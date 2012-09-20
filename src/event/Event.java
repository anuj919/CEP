package event;

import java.io.Serializable;
import java.util.Comparator;

import time.timestamp.TimeStamp;

@SuppressWarnings("serial")
public abstract class Event implements Serializable {
	public static Comparator<Event> timeBasedComparator = new Comparator<Event>() {
		@Override
		public int compare(Event e1, Event e2) {
			return e1.getTimeStamp().compareTo(e2.getTimeStamp());
		}
	};
	abstract public EventClass getEventClass();
	abstract public TimeStamp getTimeStamp();
	abstract public void setTimeStamp(TimeStamp ts);
	abstract public Object getAttributeValue(String attrName) throws NoSuchFieldException; 
	/*public static Comparator<Event> getTimeBasedComparator() {
		return timeBasedComparator;
	}*/
	abstract public boolean isConsumed();
	abstract public void setConsumed(boolean b);
}
