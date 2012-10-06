#!/bin/bash

inputEvents=50000
classes=3
duration=50
predicate="E1.a + E2.a < 5"
testcase="testcase1"

./run_stream_testcase.sh 1000 $inputEvents 1 $classes "$predicate" $duration
./run_stream_testcase.sh 1000 $inputEvents 2 $classes "$predicate" $duration
./run_stream_testcase.sh 1000 $inputEvents 3 $classes "$predicate" $duration
mkdir Results/$testcase
mv Results/OneState Results/AutomatonWithReuse Results/AutomatonWithNoReuse Results/$testcase

