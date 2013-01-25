package event.util;

import time.timemodel.IntervalTimeModel;

public class Policies {
	private static Policies instance;
	private IntervalTimeModel currentTimeModel;
	
	public static Policies getInstance() {
		if(instance==null)
			instance = new Policies();
		return instance;
	}
	
	private Policies() {
		// Hard coding this, but should read from config file
		currentTimeModel = IntervalTimeModel.getInstance();
	}
	
	public IntervalTimeModel getTimeModel() {
		return currentTimeModel; 
	}
}
