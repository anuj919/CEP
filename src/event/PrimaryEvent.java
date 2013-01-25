package event;

import java.util.HashMap;
import java.util.Map;

import time.timestamp.IntervalTimeStamp;

import event.eventtype.PrimaryEventType;
import event.util.TypeChecker;
import event.util.TypeMismatchException;

@SuppressWarnings("serial")
public class PrimaryEvent extends Event  {
	//private int eventId;
	EventClass eventClass;
	@SuppressWarnings("unchecked")
	IntervalTimeStamp timestamp;
	Map<String,Object> values; 
	boolean consumed;
	
	// this is done so that kryo can work
	private PrimaryEvent(){
		
	}
	
	/* Each query should have its own instance of Primary Event
	 * thus it should use copyOf to get its own event before submitting to automaton
	 */
	public static PrimaryEvent copyOf(PrimaryEvent e) {
		PrimaryEvent newEvent = new PrimaryEvent(e.eventClass);
		newEvent.timestamp=e.timestamp;
		newEvent.values=e.values;
		newEvent.consumed=e.consumed;
		return newEvent;
	}
	
	public PrimaryEvent(EventClass eventClass) {
		this.eventClass = eventClass;
		values = new HashMap<String, Object>();
		consumed=false;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void setTimeStamp(IntervalTimeStamp timestamp) {
		this.timestamp = timestamp;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public IntervalTimeStamp getTimeStamp() {
		return timestamp;
	}
	
	public void addAttributeValue(String attrName, Object value) throws TypeMismatchException {
		TypeChecker.checkType(value, ((PrimaryEventType)eventClass.getEventType()).getAttributeType(attrName));
		values.put(attrName, value);
	}
	
	@Override
	public Object getAttributeValue(String attrName) { //throws NoSuchFieldException {
		//if(!((PrimaryEventType)eventClass.getEventType()).containsAttribute(attrName))
		//	throw new NoSuchFieldException();
		return values.get(attrName);
	}
	
	public Map<String, Object> getAttributeValueMap() {
		return values;
	}

	@Override
	public EventClass getEventClass() {
		return eventClass;
	}
	
	
	@Override
	public String toString() {
		StringBuffer str = new StringBuffer(getEventClass().getName());
		str.append("[");
		for(String attr: values.keySet()) {
			str.append(attr);
			str.append(":");
			str.append(values.get(attr));
			str.append(",");
		}
		str.append("]@");
		str.append(timestamp.toString());
		return str.toString();
	}

	public boolean isConsumed() {
		return consumed;
	}

	public void setConsumed(boolean consumed) {
		assert consumed;
		this.consumed = consumed;
	}
}
