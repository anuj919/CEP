package datastructures;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MultiQueue<T> {
	private int numInternalQueue;
	private List<Queue<T>> lists;
	private int roundNum;

	public MultiQueue(int numInternalQueue) {
		this.numInternalQueue = numInternalQueue;
		lists = new ArrayList<Queue<T>>(numInternalQueue);
		for(int i=0;i<numInternalQueue;i++)
			lists.add(new ConcurrentLinkedQueue<T>());
		roundNum = 0;
	}
	
	public Queue<T> getList(int index) {
		return lists.get(index);
	}
	
	public int getNumInternalQueue() {
		return numInternalQueue;
	}
	
	public synchronized void add(T object) {
		lists.get(roundNum).add(object);
		roundNum=(roundNum+1)%numInternalQueue;
	}

}