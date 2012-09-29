#!/bin/bash 
if [[ $# < 2 ]]
then
    echo "Usage: $0 outputFile classToMonitor [args]"
    exit 1
fi

port=9010
ip=127.0.0.1
scala_lib=/home/test-1/Documents/eclipse/configuration/org.eclipse.osgi/bundles/433/2/.cp/lib/scala-library.jar
max_memory=3200m
interval=1

outputfile=$1
shift


java -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=$port  -Dcom.sun.management.jmxremote.authenticate=false -Djava.rmi.server.hostname=$ip -Dcom.sun.management.jmxremote.ssl=false -cp bin:$scala_lib:$(echo ../lib/*.jar | tr ' ' ':') -Xms64m -Xmx$max_memory "$@" &

sleep 1

java -cp bin monitor.MonitorUsage $ip $port $interval $1 > $outputfile



