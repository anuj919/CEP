package time.timestamp;

import java.util.Comparator;


public class IntervalTimeStamp {
	public Double start;
	public Double end;
	
	private static Comparator<IntervalTimeStamp> cachedComparator = new Comparator<IntervalTimeStamp >() {
		@Override
		public int compare(IntervalTimeStamp ts1, IntervalTimeStamp ts2) {
			
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
	
	public static  Comparator<IntervalTimeStamp > getComparator() {
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
	
	public void setStartTime(double start) {
		this.start=start;
	}
	
	public void setEndTime(double end) {
		this.end=end;
	}
	
	/* compare end time stamps of e1 and e2,
	 * if e1.end < e2.end => e1 < e2
	 * if e1.end = e2.end && e1.start < e2.start => e1 < e2
	 * if e1.end = e2.end && e1.start = e2.start => e1 = e2
	 * if e1.end > e2.end => e1 > e2
	 */
	
	public int compareTo(IntervalTimeStamp ots) {		
		return getComparator().compare(this, ots);
	}


	public IntervalTimeStamp copy() {
		return new IntervalTimeStamp(this.start,this.end);
	}	
	
	@Override
	public String toString() {
		return "["+start+","+end+"]";
	}
}