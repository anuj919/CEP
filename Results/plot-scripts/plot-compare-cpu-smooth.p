set term postscript enhanced color
#set datafile separator ";"

set ylabel "CPU Used (%)"
set xlabel "Time Elapsed (s)"
set yrange [0:100*8]

set output 'plot-compare-cpu-smooth.ps'
plot "OneState/result.csv" using 5:4 title 'OneState' with lp smooth bezier,\
"AutomatonWithReuse/result.csv" using 5:4 title 'AutomatonWithReuse' with lp smooth bezier, \
"AutomatonWithNoReuse/result.csv" using 5:4 title 'AutomatonWithNoReuse' with lp smooth bezier
