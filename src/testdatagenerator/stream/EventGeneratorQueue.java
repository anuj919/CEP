package testdatagenerator.stream;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.math3.distribution.IntegerDistribution;

import time.timestamp.IntervalTimeStamp;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;

import event.PrimaryEvent;

public class EventGeneratorQueue implements Runnable {
	BlockingQueue<PrimaryEvent> communicationLink;
	GenerateRandomEventStream generator;
	IntegerDistribution timeStampDist;
	long time;
	
	EventGeneratorQueue(GenerateRandomEventStream generator, 
						IntegerDistribution timeStampDist, int queueSize) {
		this.generator = generator;
		this.timeStampDist = timeStampDist;
		this.communicationLink = new LinkedBlockingQueue<PrimaryEvent>(queueSize);
	}
	
	BlockingQueue<PrimaryEvent> getOutputQueue() {
		return communicationLink;
	}

	@Override
	public void run() {
		for(;;) {
			PrimaryEvent e = generator.generateEvent();
			time+=timeStampDist.sample();
			time++;
			e.setTimeStamp(new IntervalTimeStamp(time,time));
			try {
				communicationLink.put(e);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
				return;
			}
		}
	}

	public long getTime() {
		return ((IntervalTimeStamp) communicationLink.peek().getTimeStamp()).getEndTime();
	}
}
