package event;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import scala.Tuple2;
import scala.collection.JavaConversions;
import scala.collection.immutable.*;
import time.timestamp.IntervalTimeStamp;
import time.timestamp.TimeStamp;
import event.util.Policies;



@SuppressWarnings("serial")
public class ComplexEvent extends Event  {
	//private int eventId;
	protected EventClass eventClass;
	protected TimeStamp timestamp;
	protected scala.collection.immutable.List<Event> constituents; 
	protected java.util.List<Event> constituentsInJava;
	protected scala.collection.immutable.List<String> constitutingEventClasses;
	
	protected TimeStamp permissibleWindow;
	//private Event endEvent;
	private static enum endsHow {EVENT, DEADLINE};
	protected endsHow endsBy;
	private boolean consumed;

	private AtomicInteger atomicInt;
	
	
	
	public static ComplexEvent copyOf(ComplexEvent ce) {
		ComplexEvent newEvent = new ComplexEvent(ce.getEventClass());
		newEvent.timestamp = ce.timestamp.deepCopy();
		newEvent.constituents = ce.constituents;
		newEvent.constituentsInJava = ce.constituentsInJava;
		newEvent.constitutingEventClasses = ce.constitutingEventClasses;
		newEvent.permissibleWindow = (IntervalTimeStamp)ce.permissibleWindow.deepCopy();
		newEvent.endsBy = ce.endsBy;
		newEvent.consumed=ce.consumed;
		return newEvent;
	}
	
	public ComplexEvent(EventClass eventClass) {
		this.eventClass = eventClass;
		constituents = List$.MODULE$.empty();
		constituentsInJava = Collections.emptyList();
		constitutingEventClasses = List$.MODULE$.empty();
		atomicInt = new AtomicInteger();
		consumed=false;
	}
	
	@Override
	public void setTimeStamp(TimeStamp timestamp) {
		this.timestamp = timestamp;
	}
	
	@Override
	public TimeStamp getTimeStamp() {
		return timestamp;
	}
	
	public int addEvent(Event e) {
		if (e instanceof PrimaryEvent) {
			constituents=$colon$colon$.MODULE$.apply(e,constituents);
			constituentsInJava = JavaConversions.seqAsJavaList(constituents);
			if(!constitutingEventClasses.contains(e.getEventClass().getName()))
				constitutingEventClasses = $colon$colon$.MODULE$.apply(e.getEventClass().getName(),constitutingEventClasses);
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
		return constituentsInJava;
	}
	
	private void updateTimeStamp(Event e) {
		if(timestamp == null)
			timestamp = e.getTimeStamp();
		else {
			timestamp = Policies.getInstance().getTimeModel().combine(timestamp, e.getTimeStamp());
		}
	}
	
	// user has to specify EventClass[:NthInstance].AttrName
	// NthInstance index starts from 1
	@Override
	public Object getAttributeValue(String attrSpec) throws NoSuchFieldException {
		String[] eventClassAndAttr = attrSpec.split("\\.");
		String[] eventClassAndInstance = eventClassAndAttr[0].split(":");
		
		String eventClassName = eventClassAndInstance[0];
		int nthInstance = ((eventClassAndInstance.length==2) ? Integer.parseInt(eventClassAndInstance[1]) : 1);
		String attrName = eventClassAndAttr[1];
		
		return getAttributeValue(eventClassName, nthInstance, attrName);
	}
	
	public Object getAttributeValue(String eventClassName, int nthInstance, String attrName) throws NoSuchFieldException {
		//determine the referenced event
		Iterator<Event> iterator = JavaConversions.asJavaIterator(constituents.reverseIterator());
		for(;iterator.hasNext();) {
			Event current=iterator.next();
			if(current.getEventClass().name.equals(eventClassName)) {
				nthInstance--;
				if(nthInstance==0)
					return current.getAttributeValue(attrName);
			}
		}
		return null;
	}
	

	@Override
	public EventClass getEventClass() {
		return eventClass;
	}
	
	public void setEventClass(EventClass newClass) {
		this.eventClass=newClass;
	}
	/*public void setWindowTillEvent(Event e) {
		endEvent = e;
		endsBy = endsHow.EVENT;
	}*/
	
	public void setPermissibleTimeWindowTill(TimeStamp window) {
		permissibleWindow = window;
		endsBy = endsHow.DEADLINE;
	}
	
	public TimeStamp getPermissibleTimeWindowTill() {
		return permissibleWindow;
	}
	
	public int getAuxAtomicInteger() {
		return atomicInt.get();
	}
	
	public int addAndGetAuxAtomicInteger(int i) {
		return atomicInt.addAndGet(i);
	}
	
	public boolean containsEventOfClass(String className) {
		return constitutingEventClasses.contains(className);
	}
	
	@Override
	public String toString() {
		StringBuffer str= new StringBuffer("{");
		for(Event e:JavaConversions.seqAsJavaList(constituents)) {
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
		for(Event e:constituentsInJava) {
			if(e.isConsumed())
				consumed=true;
		}
		return consumed;
	}

	public void setConsumed(boolean consumed) {
		assert consumed;
		for(Event e:constituentsInJava) {
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
	
	
}
