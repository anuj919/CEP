package evaluator;

import event.ComplexEvent;

public interface Evaluator {
	public boolean evaluate(ComplexEvent vars) throws NoSuchFieldException;
}
