set term postscript enhanced color
#set datafile separator ";"

set ylabel "CPU Used (%)"
set xlabel "Time Elapsed (s)"
#set yrange [0:1024*4.5]

set output 'plot-compare-cpu.ps'
plot "OneState/result.csv" using 5:4 title 'OneState' with lp,\
"AutomatonWithReuse/result.csv" using 5:4 title 'AutomatonWithReuse' with lp, \
"AutomatonWithNoReuse/result.csv" using 5:4 title 'AutomatonWithNoReuse' with lp


      