package evaluator;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.codehaus.janino.SimpleCompiler;

import event.AttributeType;
import event.eventtype.EventType;

public class JaninoEvalFactory {

    final static Pattern PATTERN =
        Pattern.compile("([a-zA-Z][a-zA-Z0-9.]*)");
    
    static AtomicInteger instanceId = new AtomicInteger();
    
    final static Map<AttributeType,String> conversionMap;
    
    static {
    	conversionMap = new HashMap<AttributeType, String>();
    	conversionMap.put(AttributeType.Integer, "(int)");
    	conversionMap.put(AttributeType.Double, "(double)");
    	conversionMap.put(AttributeType.String, "");
    }

    //private  SimpleCompiler compiler =
    //    new SimpleCompiler();

    public static Evaluator fromString(EventType complextype, String stringPredicate) {
    	String unique_id=Integer.toString(instanceId.incrementAndGet());
    	
    	if(stringPredicate==null)
    		return AlwaysTrueEvaluator.getInstance();
    	
    	SimpleCompiler compiler = new SimpleCompiler();
        StringBuffer varCode = new StringBuffer();
        Matcher matcher = PATTERN.matcher(stringPredicate);
        Map<String,AttributeType> names = new HashMap<String,AttributeType>();
        boolean attrNotFound=false;
        while (matcher.find() && !attrNotFound) {
            String name = matcher.group(0);
            
            String newname = name.replace('.', '_').replace(':', '_');
            if (names.containsKey(newname))
                continue;
            AttributeType attrType = null;
            try{
            	attrType=complextype.getAttributeType(name);
            } catch(NullPointerException npe) {
            	// this means predicate will never be satisfied here
            	attrNotFound=true;
            	break;
            }
            
            String[] eventClassAndAttr = name.split("\\.");
    		
    		String eventClassName = eventClassAndAttr[0];
    		String attrName = eventClassAndAttr[1];
                        
            if(attrType!=null) {
            	varCode.append(attrType.name()+" " + newname+ " = ("+attrType.name()+")vars.getAttributeValue(\"" + eventClassName+"\",\"" + attrName + "\");\n");
            }
            else
            	attrNotFound=true;
            names.put(newname,attrType);
        }
        
        String code = null;
        if(attrNotFound)
        	code = "throw new NoSuchFieldException()";
        else {
        	String modifiedPredicate = stringPredicate.replace('.', '_');
        	for(String varName:names.keySet()) {
        		modifiedPredicate= modifiedPredicate.replace(varName, conversionMap.get(names.get(varName))+varName);
        	}
        	code = varCode + "\n" + "\t\treturn "+ modifiedPredicate; 
        }
        String source = "package evaluator;\n"
         +"public class JaninoEvaluator"+unique_id+" implements evaluator.Evaluator {\n"
         +"\tpublic boolean evaluate(event.ComplexEvent vars) throws NoSuchFieldException {\n" 
         + "\t\t" + code +";\n" 
         + "\t}\n"
         +"}\n";

        try {
        	//System.err.println(source);
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