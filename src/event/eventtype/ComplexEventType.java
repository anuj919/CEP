package event.eventtype;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import event.AttributeType;
import event.EventClass;

public class ComplexEventType extends EventType {
	Map<String,EventClass> map;
	
	public ComplexEventType(Collection<EventClass> eventClasses) {
		map=new HashMap<String, EventClass>();
		for(EventClass eClass : eventClasses)
			map.put(eClass.getName(), eClass);
	}
		
	public int hashCode() {
		return map.hashCode();
	}
	
	public boolean equals(Object o) {
		if(o==null) return false;
		return this==o || ( o instanceof ComplexEventType && ((ComplexEventType)o).map.keySet().equals(this.map.keySet()));
	}

	// syntax: class.attr
	@Override
	public AttributeType getAttributeType(String attr) {
		int index = attr.lastIndexOf(".");
		String eventClass = attr.substring(0,index);
		String attrName = attr.substring(index+1);
		return map.get(eventClass).getEventType().getAttributeType(attrName);
	}
	
	@Override
	public String toString() {
		StringBuffer sbuff = new StringBuffer();
		for(String str : map.keySet()) {
			sbuff.append(str);
			sbuff.append(",");
		}
		return sbuff.toString();
	}

	public Collection<? extends EventClass> getEventClasses() {
		return map.values();
	}
}
