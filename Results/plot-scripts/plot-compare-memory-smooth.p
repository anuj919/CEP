set term postscript enhanced color
#set datafile separator ";"

set ylabel "Memory Usage (MB)"
set xlabel "Time Elapsed (s)"
set yrange [0:1024*7.5]

set output 'plot-compare-memory-smooth.ps'
plot "OneState/result.csv" using 5:2 title 'OneState' with lp smooth bezier,\
"AutomatonWithReuse/result.csv" using 5:2 title 'AutomatonWithReuse' with lp smooth bezier, \
"AutomatonWithNoReuse/result.csv" using 5:2 title 'AutomatonWithNoReuse' with lp smooth bezier


      