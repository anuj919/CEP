package event;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import time.timestamp.IntervalTimeStamp;
import time.timestamp.TimeStamp;
import event.util.Policies;


@SuppressWarnings("serial")
public class ComplexEvent extends Event  {
	//private int eventId;
	protected EventClass eventClass;
	protected TimeStamp timestamp;
	protected List<Event> constituents; 
	protected Map<String,List<Event>> eventClassToEvents;
	
	protected TimeStamp permissibleWindow;
	//private Event endEvent;
	private static enum endsHow {EVENT, DEADLINE};
	protected endsHow endsBy;
	private boolean consumed;

	private AtomicInteger atomicInt;
	
	
	
	public static ComplexEvent copyOf(ComplexEvent ce) {
		ComplexEvent newEvent = new ComplexEvent(ce.getEventClass());
		newEvent.timestamp = ce.timestamp.deepCopy();
		newEvent.constituents = new LinkedList<Event>(ce.constituents);
		newEvent.permissibleWindow = (IntervalTimeStamp)ce.permissibleWindow.deepCopy();
		newEvent.eventClassToEvents= new HashMap<String, List<Event>>();
		for(String className : ce.eventClassToEvents.keySet()) {
			List<Event> newlist= new LinkedList<Event>(ce.eventClassToEvents.get(className));
			newEvent.eventClassToEvents.put(className,newlist);
		}
		newEvent.endsBy = ce.endsBy;
		newEvent.consumed=ce.consumed;
		return newEvent;
	}
	
	public ComplexEvent(EventClass eventClass) {
		this.eventClass = eventClass;
		constituents = new LinkedList<Event>();
		atomicInt = new AtomicInteger();
		eventClassToEvents= new HashMap<String, List<Event>>();
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
			constituents.add(e);
			//if(!constitutingEventClasses.contains(e.getEventClass().getName()))
			//	constitutingEventClasses = constitutingEventClasses.plus(e.getEventClass().getName());;
			List<Event> list=eventClassToEvents.get(e.getEventClass().getName());
			if(list==null) {
				list=new LinkedList<Event>();
				list.add(e);
				eventClassToEvents.put(e.getEventClass().getName(), list);
			}
			else
				list.add(e);
			
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
	
	// nthIntance index starts from 1
	public Object getAttributeValue(String eventClassName, int nthInstance, String attrName) throws NoSuchFieldException {
		//determine the referenced event
		/*CircularFifoBuffer buffer = new CircularFifoBuffer(nthInstance);

		for(Event current:constituents) {
			if(current.getEventClass().name.equals(eventClassName)) {
				buffer.add(current);
			}
		}
		if(buffer.size()<nthInstance)
			return null;
		for(int i=0;i<nthInstance-1;i++)
			buffer.remove();
		return ((Event)buffer.remove()).getAttributeValue(attrName); */
		
		List<Event> list=eventClassToEvents.get(eventClassName);
		if(list==null || list.size()-nthInstance < 0)
			return null;
		return list.get(nthInstance).getAttributeValue(attrName);  
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
		return eventClassToEvents.containsKey(className);
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
	
	
}
