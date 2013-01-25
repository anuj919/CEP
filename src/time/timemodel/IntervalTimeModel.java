package time.timemodel;

import java.util.Comparator;
import java.util.List;

import time.timestamp.IntervalTimeStamp;

public class IntervalTimeModel {
	
	private static IntervalTimeModel instance = new IntervalTimeModel();
	
	private IntervalTimeModel() {}
	
	public static IntervalTimeModel getInstance() {
		return instance;
	}
	
	public IntervalTimeStamp  combine(IntervalTimeStamp ts1, IntervalTimeStamp ts2) {
		return new IntervalTimeStamp(ts1.getStartTime().compareTo(ts2.getStartTime()) <0 ? ts1.getStartTime() : ts2.getStartTime(), //min
				ts1.getEndTime().compareTo(ts2.getEndTime()) <0 ? ts2.getEndTime() : ts1.getEndTime()); //max
	}
	
	public void  combineInPlace(IntervalTimeStamp ts1, IntervalTimeStamp ts2) {
		double start = Math.min(ts1.getStartTime(), ts2.getStartTime()); //min
		double end = Math.max(ts1.getEndTime(),ts2.getEndTime());//max 
		ts1.setStartTime(start);
		ts1.setEndTime(end);
	}

	public IntervalTimeStamp next(IntervalTimeStamp t, List<IntervalTimeStamp > candidates) {
		// TODO Auto-generated method stub
		return null;
	}
	
	/* can event e2 with ts t2 be next event of e1 with ts t1?? */ 
	
	public boolean canBeNext(IntervalTimeStamp _ts2, IntervalTimeStamp _ts1) {
		IntervalTimeStamp ts1=(IntervalTimeStamp) _ts1;
		IntervalTimeStamp ts2=(IntervalTimeStamp) _ts2;
		return ts1.getStartTime() <= ts2.getStartTime()  && ts1.getEndTime() <= ts2.getEndTime();
	}

	public Comparator<IntervalTimeStamp> getTimeStampComparator() {
		return IntervalTimeStamp.getComparator();
	}

	public IntervalTimeStamp getWindowCompletionTimeStamp(IntervalTimeStamp startts, long duration) {
		if (startts instanceof IntervalTimeStamp) {
			IntervalTimeStamp ts = (IntervalTimeStamp) startts;
			return new IntervalTimeStamp(ts.getStartTime()+duration, ts.getStartTime()+duration);
		}
		else {
			throw new RuntimeException("Not supported type of timestamp");
		}
	}

	public IntervalTimeStamp getPointBasedTimeStamp(double time) {
		return new IntervalTimeStamp(time,time);
	}
}
