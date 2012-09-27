#!/bin/bash 
if [[ $# != 2 ]]
then
    echo "Usage: $0 classToMonitor [args]"
    exit 1
fi

scala_lib=/home/test-1/Documents/eclipse/configuration/org.eclipse.osgi/bundles/433/2/.cp/lib/scala-library.jar
max_memory=4g

java -cp bin:$scala_lib:$(echo ../lib/*.jar | tr ' ' ':') -Xms64m -Xmx$max_memory "$@"



