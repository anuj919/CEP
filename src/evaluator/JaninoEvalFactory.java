package evaluator;

import java.io.StringReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import event.AttributeType;
import event.eventtype.EventType;

import org.codehaus.janino.SimpleCompiler;

public class JaninoEvalFactory {

    final static Pattern PATTERN =
        Pattern.compile("([a-zA-Z][a-zA-Z0-9.]*)");
    
    static AtomicInteger instanceId = new AtomicInteger();

    //private  SimpleCompiler compiler =
    //    new SimpleCompiler();

    public static Evaluator fromString(EventType complextype, String stringPredicate) {
    	String unique_id=Integer.toString(instanceId.incrementAndGet());
    	
    	if(stringPredicate==null)
    		return AlwaysTrueEvaluator.getInstance();
    	
    	SimpleCompiler compiler = new SimpleCompiler();
        StringBuffer varCode = new StringBuffer();
        Matcher matcher = PATTERN.matcher(stringPredicate);
        Set<String> names = new HashSet<String>();
        boolean attrNotFound=false;
        while (matcher.find() && !attrNotFound) {
            String name = matcher.group(0);
            if (names.contains(name))
                continue;
            String newname = name.replace('.', '_').replace(':', '_');
            AttributeType attrType = null;
            try{
            	attrType=complextype.getAttributeType(name);
            } catch(NullPointerException npe) {
            	// this means predicate will never be satisfied here
            	attrNotFound=true;
            	break;
            }
            
            String[] eventClassAndAttr = name.split("\\.");
    		String[] eventClassAndInstance = eventClassAndAttr[0].split(":");
    		
    		String eventClassName = eventClassAndInstance[0];
    		int nthInstance = ((eventClassAndInstance.length==2) ? Integer.parseInt(eventClassAndInstance[1]) : 1);
    		String attrName = eventClassAndAttr[1];
                        
            if(attrType!=null)
            	varCode.append(attrType.name()+" " + newname+ " = ("+attrType.name()+")vars.getAttributeValue(\"" + eventClassName+"\","+ nthInstance +",\"" + attrName + "\");");
            else
            	attrNotFound=true;
            names.add(name);
        }
        
        String code = null;
        if(attrNotFound)
        	code = "throw new NoSuchFieldException()";
        else
        	code = varCode + "\n" + "\t\treturn "+ stringPredicate.replace('.', '_'); 

        String source = "package evaluator;\n"
         +"public class JaninoEvaluator"+unique_id+" implements evaluator.Evaluator {\n"
         +"\tpublic boolean evaluate(event.ComplexEvent vars) throws NoSuchFieldException {\n" 
         + "\t\t" + code +";\n" 
         + "\t}\n"
         +"}\n";

        try {
            compiler.cook(new StringReader(source));
            Class clss = compiler.getClassLoader().loadClass(
                    "evaluator.JaninoEvaluator"+unique_id);
            Evaluator eval = (Evaluator) clss.newInstance();
            return eval;
        } catch (Exception e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

}