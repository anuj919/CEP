package event;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.pcollections.ConsPStack;
import org.pcollections.HashTreePMap;
import org.pcollections.PMap;
import org.pcollections.PStack;

import time.timestamp.IntervalTimeStamp;
import time.timestamp.TimeStamp;
import event.util.Policies;

@SuppressWarnings("serial")
public class ComplexEventOld extends Event  {
	//private int eventId;
	protected EventClass eventClass;
	protected TimeStamp timestamp;
	protected PStack<Event> constituents; 
	protected Map<String,PStack<Event> > eventClassNamesToEvents;
	
	protected TimeStamp permissibleWindow;
	//private Event endEvent;
	private static enum endsHow {EVENT, DEADLINE};
	protected endsHow endsBy;
	private boolean consumed;

	private AtomicInteger atomicInt;
	
	
	
	public static ComplexEventOld copyOf(ComplexEventOld ce) {
		ComplexEventOld newEvent = new ComplexEventOld(ce.getEventClass());
		newEvent.timestamp = ce.timestamp.deepCopy();
		newEvent.constituents = ce.constituents;
		newEvent.eventClassNamesToEvents = ce.eventClassNamesToEvents;
		newEvent.permissibleWindow = (IntervalTimeStamp)ce.permissibleWindow.deepCopy();
		newEvent.endsBy = ce.endsBy;
		newEvent.consumed=ce.consumed;
		return newEvent;
	}
	
	public ComplexEventOld(EventClass eventClass) {
		this.eventClass = eventClass;
		constituents = ConsPStack.empty();
		eventClassNamesToEvents = HashTreePMap.empty();
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
			constituents=constituents.plus(e);
			addToMultiList(e);
			updateTimeStamp(e);
		} else {
			ComplexEventOld ce = (ComplexEventOld) e;
			List<Event> peList = ce.getConstitutingEvents();
			for(Event e1 : peList) {
				PrimaryEvent pe = (PrimaryEvent) e1;
				constituents=constituents.plus(pe);
				addToMultiList(pe);
				updateTimeStamp(pe);
			}
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
			timestamp = Policies.getInstance().getTimeModel().combine(timestamp, e.getTimeStamp());
		}
	}

	private void addToMultiList(Event e) {
		String eventClassName = e.getEventClass().getName();
		PStack<Event> list = eventClassNamesToEvents.get(eventClassName);
		if(list == null) {
			list = ConsPStack.singleton(e);
			PMap<String, PStack<Event>> origMap = (PMap<String, PStack<Event>>) eventClassNamesToEvents; 
			eventClassNamesToEvents =  origMap.plus(eventClassName, list);
		}
		else
			list=list.plus(e);
	}
	
	// user has to specify EventClass[:NthInstance].AttrName
	// NthInstance index starts from 1
	@Override
	public Object getAttributeValue(String attrSpec) throws NoSuchFieldException {
		String[] eventClassAndAttr = attrSpec.split("\\.");
		String[] eventClassAndInstance = eventClassAndAttr[0].split(":");
		
		String eventClassName = eventClassAndInstance[0];
		int nthInstance = ((eventClassAndInstance.length==2) ? Integer.parseInt(eventClassAndInstance[1]) : 1)-1;
		String attrName = eventClassAndAttr[1];
		
		//determine the referenced event
		Event event = eventClassNamesToEvents.get(eventClassName).get(nthInstance);
		
		return event.getAttributeValue(attrName);
	}
	
	public Object getAttributeValue(String eventClassName, int nthInstance, String attrName) throws NoSuchFieldException {
		//determine the referenced event
		Event event = eventClassNamesToEvents.get(eventClassName).get(nthInstance);
		return event.getAttributeValue(attrName);
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
		List<Event> listForClass = eventClassNamesToEvents.get(className);
		return listForClass!=null && !listForClass.isEmpty();
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
	
	public static Comparator<ComplexEventOld> getTimeBasedComparator() {
		return new Comparator<ComplexEventOld>() {
			@Override
			public int compare(ComplexEventOld e1, ComplexEventOld e2) {
				return e1.getTimeStamp().compareTo(e2.getTimeStamp());
			}
		};
	}
	
	
}
