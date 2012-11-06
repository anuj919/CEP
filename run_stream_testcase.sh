#!/bin/bash

# This script starts the event generator (which uses spec.txt in current directory) and runs the given
# implementation for given conjunctive query.
# It writes statistics in Results/sender.txt , Results/client_log.txt and Results/stat.txt
# It also uses Makefile in Results directory to generate plots of memory and and CPU usage and calculate 
# statistics like average processing time, # of events dropped etc., and put all data of the simulation 
# into Results/OneState or Results/AutomatonWithReuse or Results/AutomatonWithNoReuse directory, 
# depending on the implementation chosen

if [[ $# != 6 ]]
then
    echo "Usage: $0 inputEventRate noOfEvents impl# #ofEventClasses predicate duration"
    echo "impl# 1:testcase.stream.TestConcurrentState"
    echo "impl# 2:testcase.stream.TestConcurrentStateWithAutomaton"
    echo "impl# 3:testcase.stream.TestConcurrentStateWithAutomaton"
    exit 1
fi

if [[ -z "$MAX_MEMORY" ]]
then
    echo "set MAX_MEMORY variable for java"
    exit 1
fi


className[1]="testcase.stream.TestConcurrentState"
className[2]="testcase.stream.TestConcurrentStateWithAutomaton"
className[3]="testcase.stream.TestConcurrentStateWithAutomatonNoReuse"

classes[1]="E1"
classes[2]="E1 E2"
classes[3]="E1 E2 E3"
classes[4]="E1 E2 E3 E4"
classes[5]="E1 E2 E3 E4 E5"
classes[6]="E1 E2 E3 E4 E5 E6"
classes[7]="E1 E2 E3 E4 E5 E6 E7"
classes[8]="E1 E2 E3 E4 E5 E6 E7 E8"
classes[9]="E1 E2 E3 E4 E5 E6 E7 E8 E9"

inputEventRate=$1
noOfEventsInjected=$2
predicate="$5"
duration=$6
implementation=${className[$3]}

echo $inputEventRate $noOfEventsInjected >Results/sender.txt

pkill java

./run.sh testdatagenerator.stream.GenerateRandomEventStream $inputEventRate $noOfEventsInjected  2>/dev/null >> Results/sender.txt &

sleep 3

echo "Server started, starting client..."

echo "$implementation \"${classes[$4]}\" \"$predicate\" $duration" >Results/client.txt

./monitor.sh Results/result.csv $implementation "${classes[$4]}" "$predicate" $duration  2>>Results/client.txt > Results/client_log.txt

echo "Client finished"

cd Results
processed=$(grep Processing client.txt | wc -l)
echo "Processed = " $processed >stat.txt
echo "Dropped = " $(( $noOfEventsInjected - $processed )) >>stat.txt
averageTime=$(cat client.txt | grep Processing | awk '{print $6}' | awk 'BEGIN{count=0;n=0;} { count+=$0; n++; } END{ print count/n;}')
echo "Average Time = $averageTime" >>stat.txt
make $3
