set term postscript enhanced color
#set datafile separator ";"
set output 'plot-cpu.ps'
set ylabel "CPU Usage (%)"
set xlabel "Time"
set xdata time
set timefmt "%H:%M:%S"
set format x "%H:%M"
set yrange [0:400]

plot 'result.csv' using 1:4 title 'CPU Utilized' with lp 