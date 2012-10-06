#!/bin/bash 
if [[ $# < 1 ]]
then
    echo "Usage: $0 classToMonitor [args]"
    exit 1
fi

if [[ -z "$MAX_MEMORY" ]]
then
    echo "set environment variable MAX_MEMORY for java"
    exit 1
fi

max_memory=$MAX_MEMORY

java -cp bin:$(echo ../lib/*.jar | tr ' ' ':') -Xms64m -Xmx$max_memory "$@"



