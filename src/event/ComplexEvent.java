package event;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import scala.Tuple2;
import scala.collection.JavaConversions;
import scala.collection.immutable.List$;
import scala.collection.immutable.Map;
import scala.collection.immutable.Map$;
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
	protected Map<String,scala.collection.immutable.List<Event> > eventClassNamesToEvents;
	protected java.util.Map<String,scala.collection.immutable.List<Event> > eventClassNamesToEventsInJava;
	
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
		newEvent.eventClassNamesToEvents = ce.eventClassNamesToEvents;
		newEvent.eventClassNamesToEventsInJava = ce.eventClassNamesToEventsInJava;
		newEvent.permissibleWindow = (IntervalTimeStamp)ce.permissibleWindow.deepCopy();
		newEvent.endsBy = ce.endsBy;
		newEvent.consumed=ce.consumed;
		return newEvent;
	}
	
	public ComplexEvent(EventClass eventClass) {
		this.eventClass = eventClass;
		constituents = List$.MODULE$.empty();
		constituentsInJava = Collections.emptyList();
		eventClassNamesToEvents = Map$.MODULE$.empty();
		eventClassNamesToEventsInJava = Collections.emptyMap();
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
			constituents=constituents.$colon$colon(e);
			constituentsInJava = JavaConversions.seqAsJavaList(constituents);
			addToMultiList(e);
			updateTimeStamp(e);
		} else {
			ComplexEvent ce = (ComplexEvent) e;
			List<Event> peList = ce.getConstitutingEvents();
			constituents = constituents.$colon$colon$colon(ce.constituents);
			constituentsInJava = JavaConversions.seqAsJavaList(constituents);
			for(Event e1 : peList) {
				PrimaryEvent pe = (PrimaryEvent) e1;
				//constituents=constituents.$colon$colon(e1);
				addToMultiList(pe);
				updateTimeStamp(pe);
			}
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

	private void addToMultiList(Event e) {
		String eventClassName = e.getEventClass().getName();
		scala.collection.immutable.List<Event> list = null;
		if(!eventClassNamesToEvents.contains(eventClassName)) {
			list = List$.MODULE$.empty().$colon$colon(e); 
		}
		else
			list=eventClassNamesToEvents.apply(eventClassName).$colon$colon(e);
		eventClassNamesToEvents =  eventClassNamesToEvents.$plus(new Tuple2<String, scala.collection.immutable.List<Event>>(eventClassName, list));
		eventClassNamesToEventsInJava = JavaConversions.mapAsJavaMap(eventClassNamesToEvents);
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
		Event event = eventClassNamesToEventsInJava.get(eventClassName).apply(nthInstance);
		
		return event.getAttributeValue(attrName);
	}
	
	public Object getAttributeValue(String eventClassName, int nthInstance, String attrName) throws NoSuchFieldException {
		//determine the referenced event
		Event event = eventClassNamesToEventsInJava.get(eventClassName).apply(nthInstance);
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
		scala.collection.immutable.List<Event> listForClass = eventClassNamesToEventsInJava.get(className);
		return listForClass!=null && !listForClass.isEmpty();
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
