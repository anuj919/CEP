package time.timestamp;

import java.util.Comparator;


public class IntervalTimeStamp implements time.timestamp.TimeStamp {
	Double start;
	Double end;
	
	private static Comparator<TimeStamp> cachedComparator = new Comparator<TimeStamp >() {
		@Override
		public int compare(TimeStamp _ts1, TimeStamp _ts2) {
			IntervalTimeStamp ts1 = (IntervalTimeStamp) _ts1;
			IntervalTimeStamp ts2 = (IntervalTimeStamp) _ts2;
			
			if(ts1.getEndTime().compareTo(ts2.getEndTime()) <0 )
				return -1;
			else if(ts1.getEndTime().compareTo(ts2.getEndTime()) == 0) {
				if(ts1.getStartTime().compareTo(ts2.getStartTime()) < 0 )
					return -1;
				else if(ts1.getStartTime().compareTo(ts2.getStartTime()) == 0 )
					return 0;
				else
					return 1;
			}
			else
				return 1;
			
		}
	};
	
	public static  Comparator<TimeStamp > getComparator() {
		return cachedComparator;
	}
	
	private IntervalTimeStamp() {}
	
	public IntervalTimeStamp(Double start, Double end) {
		if(start.compareTo(end) > 0)
			new RuntimeException();
		this.start = start;
		this.end = end;
	}
	
	public Double getStartTime() {
		return start;
	}
	
	public Double getEndTime() {
		return end;
	}
	
	/* compare end time stamps of e1 and e2,
	 * if e1.end < e2.end => e1 < e2
	 * if e1.end = e2.end && e1.start < e2.start => e1 < e2
	 * if e1.end = e2.end && e1.start = e2.start => e1 = e2
	 * if e1.end > e2.end => e1 > e2
	 */
		
	@Override
	public int compareTo(TimeStamp ots) {		
		return getComparator().compare(this, ots);
	}


	@Override
	public TimeStamp deepCopy() {
		return new IntervalTimeStamp(this.start,this.end);
	}	
	
	@Override
	public String toString() {
		return "["+start+","+end+"]";
	}
}