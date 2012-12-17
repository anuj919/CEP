#!/bin/bash

testDuration=180 #seconds => 3 minutes
index=1
classes=5
duration=50
predicate="E3.a == E4.a && E1.a + E2.a < 5"
passwd="test"
startdir="$PWD"

export MAX_MEMORY="7g"
rm -f spec.txt
ln -s spec.txt.5classes spec.txt

do_work ()
{
    echo "OneState"
    ./drop_cache.sh
    ./run_stream_testcase.sh $rate $inputEvents 1 $classes "$predicate" $duration

    echo "AutomatonWithReuse"
    ./drop_cache.sh
    ./run_stream_testcase.sh $rate $inputEvents 2 $classes "$predicate" $duration

    echo "AutomatonWithNoReuse"
    ./drop_cache.sh
    ./run_stream_testcase.sh $rate $inputEvents 3 $classes "$predicate" $duration

    mkdir Results/$testcase
    mv Results/OneState Results/AutomatonWithReuse Results/AutomatonWithNoReuse Results/$testcase
    cd Results/$testcase
    gnuplot ../plot-scripts/plot-compare-memory.p
    gnuplot ../plot-scripts/plot-compare-memory-smooth.p
    gnuplot ../plot-scripts/plot-compare-cpu.p
    gnuplot ../plot-scripts/plot-compare-cpu-smooth.p
    cd "$startdir"
}


rate=10
inputEvents=$(( $rate * $testDuration ))
testcase="testcase$index"
index=$(( index + 1 ))
echo "Rate: $rate"

do_work

rate=20
inputEvents=$(( $rate * $testDuration ))
testcase="testcase$index"
index=$(( index + 1 ))
echo "Rate: $rate"

do_work



rate=30
inputEvents=$(( $rate * $testDuration ))
testcase="testcase$index"
index=$(( index + 1 ))
echo "Rate: $rate"

do_work


rate=50
inputEvents=$(( $rate * $testDuration ))
testcase="testcase$index"
index=$(( index + 1 ))
echo "Rate: $rate"

do_work


rate=70
inputEvents=$(( $rate * $testDuration ))
testcase="testcase$index"
index=$(( index + 1 ))
echo "Rate: $rate"

do_work


rate=100
inputEvents=$(( $rate * $testDuration ))
testcase="testcase$index"
index=$(( index + 1 ))
echo "Rate: $rate"

do_work


rate=200
inputEvents=$(( $rate * $testDuration ))
testcase="testcase$index"
index=$(( index + 1 ))
echo "Rate: $rate"

do_work


rate=300
inputEvents=$(( $rate * $testDuration ))
testcase="testcase$index"
index=$(( index + 1 ))
echo "Rate: $rate"

do_work


rate=500
inputEvents=$(( $rate * $testDuration ))
testcase="testcase$index"
index=$(( index + 1 ))
echo "Rate: $rate"

do_work


rate=800
inputEvents=$(( $rate * $testDuration ))
testcase="testcase$index"
index=$(( index + 1 ))
echo "Rate: $rate"

do_work


rate=1000
inputEvents=$(( $rate * $testDuration ))
testcase="testcase$index"
index=$(( index + 1 ))
echo "Rate: $rate"

do_work


rate=1200
inputEvents=$(( $rate * $testDuration ))
testcase="testcase$index"
index=$(( index + 1 ))
echo "Rate: $rate"

do_work


rate=1500
inputEvents=$(( $rate * $testDuration ))
testcase="testcase$index"
index=$(( index + 1 ))
echo "Rate: $rate"

do_work

rate=2000
inputEvents=$(( $rate * $testDuration ))
testcase="testcase$index"
index=$(( index + 1 ))
echo "Rate: $rate"

do_work

rate=3000
inputEvents=$(( $rate * $testDuration ))
testcase="testcase$index"
index=$(( index + 1 ))
echo "Rate: $rate"

do_work

rate=5000
inputEvents=$(( $rate * $testDuration ))
testcase="testcase$index"
index=$(( index + 1 ))
echo "Rate: $rate"

do_work


mkdir Results/5classes
mv testcase* 5classes/.
