#!/bin/bash

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

./run.sh testdatagenerator.stream.GenerateRandomEventStream $inputEventRate $noOfEventsInjected  2>/dev/null >> Results/sender.txt &

sleep 1

echo "$implementation \"${classes[$4]}\" \"$predicate\" $duration" >Results/client.txt

./monitor.sh Results/result.csv $implementation "${classes[$4]}" "$predicate" $duration  2>>Results/client.txt >/dev/null

cd Results
make $3
