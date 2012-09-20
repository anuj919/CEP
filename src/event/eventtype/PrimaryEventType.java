package event.eventtype;

import java.io.IOException;
import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import event.AttributeType;
import event.PrimaryEvent;
import event.util.Serializer;

@SuppressWarnings("serial")
public class PrimaryEventType extends EventType implements Serializable {
	Map<String,AttributeType> map;
	
	final public static PrimaryEventType emptyType = new PrimaryEventType();
	
	public PrimaryEventType() {
		map = new HashMap<String, AttributeType>();
	}
	
	public PrimaryEventType(Map<String, AttributeType> map) {
		this.map = new HashMap<String, AttributeType>(map);
	}
	
	public void addAttribute(String name, AttributeType type) {
		map.put(name, type);
	}
	
	public void removeAttribute(String name) {
		map.remove(name);
	}
	
	@Override
	public AttributeType getAttributeType(String name) {
		return map.get(name);
	}
	
	protected final Map<String, AttributeType> getAttributeTypeMap() {
		return map;
	}
	
	public Set<String> getAttributeNames() {
		return map.keySet();
	}
	
	public boolean containsAttribute(String name) {
		return map.get(name)!=null;
	}
	
	public void addAttributes(String[] names, AttributeType[] types) {
		for(int i=0;i<names.length;i++) {
			map.put(names[i], types[i]);
		}
	}
		
	public boolean equal(Object o) {
		if(o == this) return true;
		return (o!=null && o instanceof PrimaryEventType &&
				map.keySet().equals(((PrimaryEventType)o).map.keySet())); 
	}
	
	public int hashCode(){
		return map.hashCode();
	}
	
	@Override
	public String toString() {
		StringBuffer ret=new StringBuffer("[");
		for(String name:map.keySet()) {
			ret.append(name).append(":").append(map.get(name)).append(",");
		}
		ret.append("]");
		return ret.toString();
	}
}
