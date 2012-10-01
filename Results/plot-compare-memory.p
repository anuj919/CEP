set term postscript enhanced color
#set datafile separator ";"

set ylabel "Memory Usage (MB)"
set xlabel "Time Elapsed"
#set yrange [0:1024*4.5]

set output 'plot-compare-memory.ps'
plot 'OneState/result.csv' using 5:2 title 'OneState' with lp,\
'AutomatonWithReuse/result.csv' using 5:2 title 'AutomatonWithReuse' with lp, \
'AutomatonWithNoReuse/result.csv' using 5:2 title 'AutomatonWithNoReuse' with lp


      