#!/bin/bash 

# This scripts runs the given class with given commandline arguments
# and opens the management port to which MonitorUsage.java reads
# This scripts also starts MonitorUsage after starting given class

if [[ $# < 2 ]]
then
    echo "Usage: $0 outputFile classToMonitor [args]"
    exit 1
fi

port=9010
ip=127.0.0.1
max_memory=$MAX_MEMORY
interval=1

outputfile=$1
shift


java -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=$port  -Dcom.sun.management.jmxremote.authenticate=false -Djava.rmi.server.hostname=$ip -Dcom.sun.management.jmxremote.ssl=false -cp bin:$(echo ../lib/*.jar | tr ' ' ':') -Xms64m -Xmx$max_memory -Xloggc:Results/gc_log.txt -XX:+PrintGCDetails "$@" &

sleep 1

java -cp bin monitor.MonitorUsage $ip $port $interval $1 > $outputfile



