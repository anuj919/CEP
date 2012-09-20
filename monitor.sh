#!/bin/bash

#pid=$(ps aux | grep java | tail -2  | head -1 | awk '{print $2}')
out=$(top -b -n 1 -p "$1" | tail -n 2 | head -n 1 | awk '{print $1, $12, $9}')

echo "$out%"
