#!/bin/bash 
if [[ $# < 1 ]]
then
    echo "Usage: $0 classToMonitor [args]"
    exit 1
fi

max_memory=1g

java -cp bin:$(echo ../lib/*.jar | tr ' ' ':') -Xms64m -Xmx$max_memory "$@"



