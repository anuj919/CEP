#!/bin/bash

echo $1 | head -c -2 | tail -c +2 | tr ',' '\n' | tr '=' '\t' | sed 's/ //g' | sort -n -k1 > data.txt

cat << EOF1 >plot.p
set term postscript enhanced color
set output "dist.ps"
set style fill solid
plot "data.txt" using 2:xtic(1) with histogram
EOF1

gnuplot plot.p
evince dist.ps
cat data.txt
rm data.txt
#rm dist.ps
rm plot.p


