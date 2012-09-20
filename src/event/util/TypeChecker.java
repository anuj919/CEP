package event.util;

import event.AttributeType;

public class TypeChecker {
	public static void checkType(Object o, AttributeType t) throws TypeMismatchException {
		AttributeType objType;
		if(o instanceof Integer) objType = AttributeType.Integer;
		else if(o instanceof String) objType = AttributeType.String;
		else if(o instanceof Double) objType = AttributeType.Double;
		else
			objType = AttributeType.UNKNOWN;
		
		if(t == null || objType==AttributeType.UNKNOWN || objType!=t)
			throw new TypeMismatchException(t, objType);
	}
	
}
