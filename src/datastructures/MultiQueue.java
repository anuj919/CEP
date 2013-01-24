package datastructures;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class MultiQueue<T> {
	private int numInternalQueue;
	private List<List<T>> lists;
	private int roundNum;

	public MultiQueue(int numInternalQueue) {
		this.numInternalQueue = numInternalQueue;
		lists = new ArrayList<List<T>>(numInternalQueue);
		for(int i=0;i<numInternalQueue;i++)
			lists.add(new LinkedList<T>());
		roundNum = 0;
	}
	
	public List<T> getList(int index) {
		return lists.get(index);
	}
	
	public int getNumInternalQueue() {
		return numInternalQueue;
	}
	
	public void add(T object) {
		lists.get(roundNum).add(object);
		roundNum=(roundNum+1)%numInternalQueue;
	}

}