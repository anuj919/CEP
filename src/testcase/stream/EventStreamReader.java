package testcase.stream;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;

import event.PrimaryEvent;

public class EventStreamReader implements Runnable {
	Kryo kryo;
	Input input;
	BlockingQueue<PrimaryEvent> communicationLink;
	long receivedCount, droppedCount;
	
	EventStreamReader(Kryo kryo, Input input, int queueSize) {
		this.kryo = kryo;
		this.input = input;
		this.communicationLink = new LinkedBlockingQueue<PrimaryEvent>(queueSize);
		this.receivedCount=0;
		this.droppedCount=0;
	}
	
	BlockingQueue<PrimaryEvent> getInputQueue() {
		return communicationLink;
	}

	@Override
	public void run() {
		for(;;) {
			PrimaryEvent e = kryo.readObject(input, PrimaryEvent.class);
			boolean added = communicationLink.offer(e);
			if(!added) {
				droppedCount++;
				System.out.println("Dropping event "+(receivedCount+1));
			}
			receivedCount++;
		}
	}

	public long getDropCount() {
		return droppedCount;
	}
	
	public long getTotalRecievedCount() {
		return receivedCount;
	}
}
