package evaluator;
import java.util.LinkedList;

import time.timestamp.IntervalTimeStamp;

import event.AttributeType;
import event.ComplexEvent;
import event.EventClass;
import event.PrimaryEvent;
import event.eventtype.ComplexEventType;
import event.eventtype.PrimaryEventType;
import event.util.TypeMismatchException;


public class TestJanino {

	
	public static void main(String[] args) throws NoSuchFieldException {
		int testcases=1000;
		
		
		
		ComplexEvent ce = getComplexEvent();
		
		long start=System.nanoTime();
		Evaluator evaluator =null;
		evaluator = JaninoEvalFactory.fromString(ce.getEventClass().getEventType(),"(E1.a1 + E2.a2 <20 ) &&  (E1.price + E2.price > 20)");
		long end = System.nanoTime();
		System.out.println("parsing "+(end-start));
		
		start=System.nanoTime();
		for(int i=0;i<testcases;i++) {
			try {
				evaluator.evaluate(ce);
			} catch (NullPointerException ne) {
				end = System.nanoTime();
				break;
			}
		}
		//end = System.nanoTime();
		System.out.println(end-start);
		//System.out.println("evaluating "+(end-start)/testcases);
		//System.out.println(evaluator.evaluate(ce));

	}


	private static ComplexEvent getComplexEvent() {
		PrimaryEventType et1 = new PrimaryEventType();
		et1.addAttribute("a1", AttributeType.Integer);
		et1.addAttribute("price", AttributeType.Integer);
		EventClass ec1 = new EventClass("E1",et1);
		
		PrimaryEventType et2 = new PrimaryEventType();
		et2.addAttribute("a2", AttributeType.Integer);
		et2.addAttribute("price", AttributeType.Integer);
		EventClass ec2 = new EventClass("E2",et2);
		
		LinkedList<EventClass> list = new LinkedList<EventClass>();
		list.add(ec1);
		list.add(ec2);
		
		ComplexEventType ce_type = new ComplexEventType(list);
		EventClass ce_class = new EventClass("CE1",ce_type);
		
		PrimaryEvent e1 = new PrimaryEvent(ec1);
		PrimaryEvent e2 = new PrimaryEvent(ec1);
		PrimaryEvent e3 = new PrimaryEvent(ec2);
		e1.setTimeStamp(new IntervalTimeStamp(0l, 0l));
		e2.setTimeStamp(new IntervalTimeStamp(0l, 0l));
		e3.setTimeStamp(new IntervalTimeStamp(0l, 0l));
		try {
			//e1.addAttributeValue("a1", 10);
			e1.addAttributeValue("price", 100);
			e2.addAttributeValue("price", 200);
			e3.addAttributeValue("a2", 10);
			//e2.addAttributeValue("price", 20);
		} catch (TypeMismatchException e) {
			e.printStackTrace();
		}
		
		ComplexEvent ce = new ComplexEvent(ce_class);
		ce.addEvent(e1);
		ce.addEvent(e2);
		ce.addEvent(e3);
		ce.setTimeStamp(new IntervalTimeStamp(0l, 0l));
		try {
			ce.getAttributeValue("E1.a1");
			ce.getAttributeValue("E1",1,"a1");
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		long start=System.nanoTime();
		ComplexEvent ce2= new ComplexEvent(ce_class);
		ce2.addEvent(ce);
		System.out.println(System.nanoTime()-start);
		System.out.println(ce2);
		
		return ce;
	}

}


