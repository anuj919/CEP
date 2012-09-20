package event.util;

import event.AttributeType;

@SuppressWarnings("serial")
public class TypeMismatchException extends Exception {
	private AttributeType expected, given;
	public TypeMismatchException(AttributeType expected, AttributeType given) {
		this.expected = expected;
		this.given = given;
	}
	
	public AttributeType getExpectedType() {
		return expected;
	}
	
	public AttributeType getGivenType() {
		return given;
	}
	
	@Override
	public String getLocalizedMessage(){
		return "Expected: "+expected+" Given: "+given;
	}
}
