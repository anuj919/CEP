package time.timemodel;

import java.util.Comparator;
import java.util.List;

import time.timestamp.TimeStamp;

abstract public class TimeModel {
	abstract public TimeStamp next(TimeStamp t, List<TimeStamp > candidates);
	abstract public TimeStamp combine(TimeStamp ts1, TimeStamp ts2);
	abstract public TimeStamp getWindowCompletionTimeStamp(TimeStamp startts, long duration);
	abstract public TimeStamp getPointBasedTimeStamp(long time);
	abstract public Comparator<TimeStamp> getTimeStampComparator();
	abstract public boolean canBeNext(TimeStamp t1, TimeStamp t2);
}
