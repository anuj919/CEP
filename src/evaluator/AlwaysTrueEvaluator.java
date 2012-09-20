package evaluator;

import event.ComplexEvent;

public class AlwaysTrueEvaluator implements Evaluator {
	
	private static AlwaysTrueEvaluator instance;
	
	private AlwaysTrueEvaluator() {}
	
	public static AlwaysTrueEvaluator getInstance() {
		if(instance==null)
			instance = new AlwaysTrueEvaluator();
		return instance;
	}

	@Override
	public boolean evaluate(ComplexEvent vars) throws NoSuchFieldException {
		return true;
	}

}
