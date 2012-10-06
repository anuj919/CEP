package testcase.stream;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.LinkedTransferQueue;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryo.io.Input;

import event.PrimaryEvent;

public class EventStreamReader implements Runnable {
	Kryo kryo;
	Input input;
	int rate;
	BlockingQueue<PrimaryEvent> communicationLink;
	long receivedCount, droppedCount;
	
	EventStreamReader(Kryo kryo, Input input, int queueSize, int rate) {
		this.kryo = kryo;
		this.input = input;
		this.communicationLink = new ArrayBlockingQueue(queueSize, true);
		this.rate = rate;
		this.receivedCount=0;
		this.droppedCount=0;
	}
	
	BlockingQueue<PrimaryEvent> getInputQueue() {
		return communicationLink;
	}

	@Override
	public void run() {
		for(;;) {
			long t1=System.nanoTime();
			ArrayList<PrimaryEvent> list=null;
			try{
				list=kryo.readObject(input, ArrayList.class);
			} catch(KryoException ke) {
				break;
			}
			long t2=System.nanoTime();
			//System.err.println("Desrialization time= "+(t2-t1)/list.size());
			for(int i=0;i<list.size();i++) {
				long start=System.nanoTime();
				if(receivedCount%1000==0)
					System.out.println("                            Recieved="+receivedCount+" Dropped="+droppedCount);
				PrimaryEvent e =list.get(i);
				boolean added = communicationLink.offer(e);
				if(!added) {
					droppedCount++;
					//System.out.println("Dropping event "+(receivedCount+1));
				}
				receivedCount++;
				long end=System.nanoTime();
				int sleepTime=1000000000/rate - (int)(end-start);
				if(sleepTime>0) {
					try {
						Thread.sleep(sleepTime/1000000);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
						return;
					}
				}else 
					System.out.println(1000000000/rate + "<"+(end-start)+" not sleeping");
			}
			
		}
	}

	public long getDropCount() {
		return droppedCount;
	}
	
	public long getTotalRecievedCount() {
		return receivedCount;
	}
}
