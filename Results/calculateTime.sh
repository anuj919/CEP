#!/bin/bash

file="$1"

ts1=$(head -2 $file | tail -1 | awk '{print $1}')
ts2=$(tail -1 $file | awk '{print $1}')

h1=$(echo $ts1 | cut -d':' -f1)
m1=$(echo $ts1 | cut -d':' -f2)
s1=$(echo $ts1 | cut -d':' -f3)

h2=$(echo $ts2 | cut -d':' -f1)
m2=$(echo $ts2 | cut -d':' -f2)
s2=$(echo $ts2 | cut -d':' -f3)

expression="$h2*3600 + $m2*60 + $s2 - ($h1*3600 + $m1*60 + $s1)"

echo $expression | bc

