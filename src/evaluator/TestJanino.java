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
		int testcases=1;
		
		
		
		ComplexEvent ce = getComplexEvent();
		
		long start=System.nanoTime();
		Evaluator evaluator =null;
		evaluator = JaninoEvalFactory.fromString(ce.getEventClass().getEventType(),"E1.a + E2.a <5");
		long end = System.nanoTime();
		System.out.println("parsing "+(end-start));
		
		start=System.nanoTime();
		boolean result=false;
		for(int i=0;i<testcases;i++) {
			try {
				result=evaluator.evaluate(ce);
			} catch (NullPointerException ne) {
				end = System.nanoTime();
				break;
			} finally {
				end = System.nanoTime();
			}
		}
		//end = System.nanoTime();
		System.out.println(end-start);
		//System.out.println("evaluating "+(end-start)/testcases);
		//System.out.println(evaluator.evaluate(ce));

	}


	private static ComplexEvent getComplexEvent() {
		PrimaryEventType et1 = new PrimaryEventType();
		et1.addAttribute("a", AttributeType.Double);
		et1.addAttribute("price", AttributeType.Integer);
		EventClass ec1 = new EventClass("E1",et1);
		
		PrimaryEventType et2 = new PrimaryEventType();
		et2.addAttribute("a", AttributeType.Double);
		et2.addAttribute("price", AttributeType.Integer);
		EventClass ec2 = new EventClass("E2",et2);
		
		PrimaryEventType et3 = new PrimaryEventType();
		et3.addAttribute("a", AttributeType.Integer);
		et3.addAttribute("price", AttributeType.Integer);
		EventClass ec3 = new EventClass("E3",et3);
		
		PrimaryEventType et4 = new PrimaryEventType();
		et4.addAttribute("a", AttributeType.Integer);
		et4.addAttribute("price", AttributeType.Integer);
		EventClass ec4 = new EventClass("E4",et4);
		
		LinkedList<EventClass> list = new LinkedList<EventClass>();
		list.add(ec1);
		list.add(ec2);
		list.add(ec3);
		list.add(ec4);
		
		ComplexEventType ce_type = new ComplexEventType(list);
		EventClass ce_class = new EventClass("CE1",ce_type);
		
		PrimaryEvent e1 = new PrimaryEvent(ec1);
		PrimaryEvent e2 = new PrimaryEvent(ec2);
		PrimaryEvent e3 = new PrimaryEvent(ec3);
		e1.setTimeStamp(new IntervalTimeStamp(0l, 0l));
		e2.setTimeStamp(new IntervalTimeStamp(0l, 0l));
		e3.setTimeStamp(new IntervalTimeStamp(0l, 0l));
		try {
			//e1.addAttributeValue("a1", 10);
			e1.addAttributeValue("a", 2.693634799645854);
			e2.addAttributeValue("a", 1.2031368292642761);
			e3.addAttributeValue("a", 1);
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
			System.out.println(ce.getAttributeValue("E1.a"));
			System.out.println(ce.getAttributeValue("E2",1,"a"));
			System.out.println(ce.getAttributeValue("E3",1,"a"));
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return ce;
	}

}


