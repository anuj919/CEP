package monitor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

public class MonitorUsage
{
	private static MBeanServerConnection connection;
	private static JMXConnector connector;
	private static DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
	private static DecimalFormat df = new DecimalFormat("0.##");

public static void Connection(String hostname, String port) throws IOException
	{
	JMXServiceURL address = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://"+hostname+":"+port+"/jmxrmi");
	connector = JMXConnectorFactory.connect(address,null);
	connection = connector.getMBeanServerConnection();
//	System.out.println("GOT THE MBeanServerConnection---SUCCESSFULLY");
	}

private static void getHeapMemoryUsage() throws Exception
{
	ObjectName memoryMXBean=new ObjectName("java.lang:type=Memory");
	CompositeDataSupport dataSenders = (CompositeDataSupport) connection.getAttribute(memoryMXBean,"HeapMemoryUsage");
	if (dataSenders != null)
	  {
		Long commited = (Long) dataSenders.get("committed");
		Long used = (Long) dataSenders.get("used");
		System.out.print(dateFormat.format(new Date())+" "+df.format(used/(1024*1024.0))+" "+df.format(commited/(1024*1024.0))+" ");
	   }
}

private static String getCPUUsage(String name) throws Exception
{
	String[] cmd = {
	"/bin/sh",
	"-c",
	"ps -eo pcpu,pid,user,args | grep java | grep "+name+ " | head -1 | awk '{print $1}' "
	};
	Runtime run = Runtime.getRuntime();
	Process pr = run.exec(cmd);
	pr.waitFor();
	BufferedReader buf = new BufferedReader(new InputStreamReader(pr.getInputStream()));
	String line = "";
	while ((line=buf.readLine())!=null) {
		System.out.println(line);
	}
	return "NULL";
}

public static void main(String[] args) throws Exception
     {
	if(args.length!=4) {
		System.out.println("Usage: java MonitorUsage managementIP managementPort intervalInSeconds ClassNameToMonitor");
		System.out.println("Output format: HH:MM:SS UsedHeap(MB) CommitedHeap(MB) CPUPercent(%)");
		System.out.println("Options required in monitored applications: ");
		System.out.println("-Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=PORT  -Dcom.sun.management.jmxremote.authenticate=false -Djava.rmi.server.hostname=127.0.0.1 -Dcom.sun.management.jmxremote.ssl=false");
		return;
	}
	String hostname = args[0];
	String port = args[1];
	try {
	    Connection(hostname, port);
	} catch(Exception e) {
	    System.out.println("Can not connect at given port");
	    return;
	}
	int period = Integer.parseInt(args[2]);
	String name = args[3];
	System.out.println("#Time(HH:MM:SS) UsedMemory(MB) ComittedMemory(MB) CPU(%)");
	try {
	    for(;;) {
		//doGarbageCollection();                // --&gt; use this method if you want to perform Garbage Collection
		getHeapMemoryUsage();
		getCPUUsage(name);
		Thread.sleep(period * 1000);
	    }
	}catch (Exception e) {
	} finally {
	    try {
		connector.close();
	    } catch(Exception e) {}
	}
	//connector.close();
     }
}


