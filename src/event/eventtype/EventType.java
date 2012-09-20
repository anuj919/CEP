package event.eventtype;

import java.util.Map;
import java.util.Set;

import event.AttributeType;

abstract public class EventType {
	abstract public AttributeType getAttributeType(String attr);
//	abstract public Set<String> getAttributeNames();
//	abstract protected Map<String, AttributeType> getAttributeTypeMap(); 
}
