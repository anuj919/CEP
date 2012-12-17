set term postscript enhanced color
#set datafile separator ";"
set output 'plot-memory.ps'
set ylabel "Memory Usage (MB)"
set xlabel "Time"
set xdata time
set timefmt "%H:%M:%S"
set format x "%H:%M"
set yrange [0:1024*7]

plot 'result.csv' using 1:3 title 'Allocated Heap Size' with filledcurves x1 lt 1 lc rgb "#802020" ,\
   'result.csv' using 1:2 title 'Actual Usage' with filledcurves x1 lt 1 lc rgb "blue"
      