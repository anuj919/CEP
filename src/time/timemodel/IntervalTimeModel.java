package time.timemodel;

import java.util.Comparator;
import java.util.List;

import time.timestamp.IntervalTimeStamp;
import time.timestamp.TimeStamp;

public class IntervalTimeModel extends TimeModel {
	
	private static IntervalTimeModel instance = new IntervalTimeModel();
	
	private IntervalTimeModel() {}
	
	public static TimeModel getInstance() {
		return instance;
	}
	
	@Override
	public TimeStamp  combine(TimeStamp _ts1, TimeStamp _ts2) {
		IntervalTimeStamp ts1=(IntervalTimeStamp) _ts1;
		IntervalTimeStamp ts2=(IntervalTimeStamp) _ts2;
		return new IntervalTimeStamp(ts1.getStartTime().compareTo(ts2.getStartTime()) <0 ? ts1.getStartTime() : ts2.getStartTime(), //min
				ts1.getEndTime().compareTo(ts2.getEndTime()) <0 ? ts2.getEndTime() : ts1.getEndTime()); //max
	}

	@Override
	public TimeStamp next(TimeStamp t, List<TimeStamp > candidates) {
		// TODO Auto-generated method stub
		return null;
	}
	
	/* can event e2 with ts t2 be next event of e1 with ts t1?? */ 
	
	@Override
	public boolean canBeNext(TimeStamp _ts2, TimeStamp _ts1) {
		IntervalTimeStamp ts1=(IntervalTimeStamp) _ts1;
		IntervalTimeStamp ts2=(IntervalTimeStamp) _ts2;
		return ts1.getStartTime() < ts2.getStartTime()  && ts1.getEndTime() <= ts2.getEndTime();
	}

	@Override
	public Comparator<TimeStamp> getTimeStampComparator() {
		return IntervalTimeStamp.getComparator();
	}

	@Override
	public TimeStamp getWindowCompletionTimeStamp(TimeStamp startts, long duration) {
		if (startts instanceof IntervalTimeStamp) {
			IntervalTimeStamp ts = (IntervalTimeStamp) startts;
			return new IntervalTimeStamp(ts.getStartTime()+duration, ts.getStartTime()+duration);
		}
		else {
			throw new RuntimeException("Not supported type of timestamp");
		}
	}

	@Override
	public TimeStamp getPointBasedTimeStamp(long time) {
		return new IntervalTimeStamp(time,time);
	}
}
