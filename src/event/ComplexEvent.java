package event;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.pcollections.ConsPStack;
import org.pcollections.HashTreePSet;
import org.pcollections.PSet;
import org.pcollections.PStack;

import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;

import time.timemodel.IntervalTimeModel;
import time.timestamp.IntervalTimeStamp;
import event.util.Policies;


@SuppressWarnings("serial")
public class ComplexEvent extends Event  {
	//private int eventId;
	protected EventClass eventClass;
	protected IntervalTimeStamp timestamp;
	protected PStack<Event> constituents; 
	protected int constitutingEventClasses;
	
	protected double permissibleWindow;
	//private Event endEvent;
	private static enum endsHow {EVENT, DEADLINE};
	protected endsHow endsBy;
	private boolean consumed;	
	
	public static ComplexEvent copyOf(ComplexEvent ce) {
		ComplexEvent newEvent = new ComplexEvent(ce.getEventClass());
		newEvent.timestamp = ce.timestamp.copy();
		newEvent.constituents = ce.constituents;
		newEvent.constitutingEventClasses = ce.constitutingEventClasses;
		newEvent.permissibleWindow = ce.permissibleWindow;
		newEvent.endsBy = ce.endsBy;
		newEvent.consumed=ce.consumed;
		return newEvent;
	}
	
	public ComplexEvent(EventClass eventClass) {
		this.eventClass = eventClass;
		constituents = ConsPStack.empty();
		constitutingEventClasses = 0;
		consumed=false;
	}
	
	@Override
	public void setTimeStamp(IntervalTimeStamp timestamp) {
		this.timestamp = timestamp;
	}
	
	@Override
	public IntervalTimeStamp getTimeStamp() {
		return timestamp;
	}
	
	public int addEvent(Event e) {
		if (e instanceof PrimaryEvent) {
			constituents=constituents.plus(e);
			//if(!constitutingEventClasses.contains(e.getEventClass().getName()))
			//	constitutingEventClasses = constitutingEventClasses.plus(e.getEventClass().getName());;
			constitutingEventClasses ^=  e.getEventClass().getName().hashCode();
			updateTimeStamp(e);
		} else {
			ComplexEvent ce = (ComplexEvent) e;
			List<Event> peList = ce.getConstitutingEvents();
			for(Event pe: peList)
				addEvent(pe);
		}
		return constituents.size();
	}
	
	public List<Event> getConstitutingEvents() {
		return constituents;
	}
	
	private void updateTimeStamp(Event e) {
		if(timestamp == null)
			timestamp = e.getTimeStamp();
		else {
			timestamp.start = (Math.min(timestamp.start,e.getTimeStamp().start));
			timestamp.end = (Math.max(timestamp.end,e.getTimeStamp().end));
			//IntervalTimeModel.getInstance().combineInPlace(timestamp, e.getTimeStamp());
		}
	}
	
	// user has to specify EventClass[:NthInstance].AttrName
	// NthInstance index starts from 1
	@Override
	public Object getAttributeValue(String attrSpec) throws NoSuchFieldException {
		String[] eventClassAndAttr = attrSpec.split("\\.");
		String eventClassName = eventClassAndAttr[0];
		String attrName = eventClassAndAttr[1];
		return getAttributeValue(eventClassName,attrName);
	}
	
	public Object getAttributeValue(String eventClassName, String attrName) throws NoSuchFieldException {
		for(Event ce : constituents) 
			if(eventClassName.equals(ce.getEventClass().getName()))
				return ce.getAttributeValue(attrName);
		return null;  
	}
	
	@Override
	public EventClass getEventClass() {
		return eventClass;
	}
	
	public void setEventClass(EventClass newClass) {
		this.eventClass=newClass;
	}
	
	public void setPermissibleTimeWindowTill(double window) {
		permissibleWindow = window;
	}
	
	public double getPermissibleTimeWindowTill() {
		return permissibleWindow;
	}
		
	public boolean containsEventOfClass(String className) {
		for(Event ce : constituents)
			if(ce.getEventClass().getName().equals(className))
				return true;
		return false;
	}
	
	@Override
	public String toString() {
		StringBuffer str= new StringBuffer("{");
		for(Event e:constituents) {
			str.append(e);
			str.append(",");
		}
		str.append("}@");
		str.append(timestamp.toString());
		return str.toString();
	}

	public boolean isConsumed() {
		if(consumed)
			return true;
		// otherwise check all constituents
		for(Event e:constituents) {
			if(e.isConsumed())
				consumed=true;
		}
		return consumed;
	}

	public void setConsumed(boolean consumed) {
		assert consumed;
		for(Event e:constituents) {
			e.setConsumed(true);
		}
		this.consumed = consumed;
	}
	
	public static Comparator<ComplexEvent> getTimeBasedComparator() {
		return new Comparator<ComplexEvent>() {
			@Override
			public int compare(ComplexEvent e1, ComplexEvent e2) {
				return e1.getTimeStamp().compareTo(e2.getTimeStamp());
			}
		};
	}
	
	public int getEventClassesAlreadyPresent(){
		return constitutingEventClasses; 
	}
	
}
