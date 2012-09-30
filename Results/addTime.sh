#!/bin/bash

if [[ $# != 1 ]]
then
    echo "Usage: $0 resuls_file"
    exit 1
fi

file="$1"
directory=$(dirname $file)
newfilename=$(basename $file .csv)"_NoTimeElapsed.csv"

cp $file $directory/$newfilename

first=1
ts1=$(head -2 $file | tail -1 |  awk '{print $1}')
h1=$(echo $ts1 | cut -d':' -f1)
m1=$(echo $ts1 | cut -d':' -f2)
s1=$(echo $ts1 | cut -d':' -f3)



while read line
do
    if [[ $first == 1 ]]
    then
	echo $line "TimeElapsed(s)"
	first=0
	continue
    fi
    ts2=$(echo $line | awk '{print $1}')
    h2=$(echo $ts2 | cut -d':' -f1)
    m2=$(echo $ts2 | cut -d':' -f2)
    s2=$(echo $ts2 | cut -d':' -f3)

    expression="$h2*3600 + $m2*60 + $s2 - ($h1*3600 + $m1*60 + $s1)"
    timeElapsed=$(echo $expression | bc)

    echo $line  $timeElapsed
    
done >$file < $directory/$newfilename

