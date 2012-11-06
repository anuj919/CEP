package event.eventtype;

import event.AttributeType;

abstract public class EventType {
	abstract public AttributeType getAttributeType(String attr);
//	abstract public Set<String> getAttributeNames();
//	abstract protected Map<String, AttributeType> getAttributeTypeMap(); 
}
