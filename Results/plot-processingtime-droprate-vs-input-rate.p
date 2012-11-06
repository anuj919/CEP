set term postscript enhanced color

set xlabel "Event Rate (events/s)"
set ylabel "Processing Time (us/event)"
set y2label "Drop Rate (%)"
set xrange [0:3500]
set y2tics
set y2range [0:100]
set ytics nomirror

set output 'plot-processingtime-droprate-vs-input-rate.ps'

plot '< grep "OneState" results.txt' using 2:9 title 'OneState' with lp lc 'black' lt 1 pt 1, \
   '< grep "AutomatonWithReuse" results.txt' using 2:9 title 'AutomatonWithReuse' with lp lc rgb 'blue' lt 2 pt 1, \
      '< grep "AutomatonWithNoReuse" results.txt' using 2:9 title 'AutomatonWithNoReuse' with lp lc rgb 'red' lt 3 pt 1,\
	 '< grep "OneState" results.txt' using 2:8 axis x1y2 title 'OneState' with lp lc 'black' lt 1 pt 5, \
   '< grep "AutomatonWithReuse" results.txt' using 2:8 axis x1y2 title 'AutomatonWithReuse' with lp lc rgb 'blue' lt 2 pt 5, \
      '< grep "AutomatonWithNoReuse" results.txt' using 2:8 axis x1y2 title 'AutomatonWithNoReuse' with lp lc rgb 'red' lt 3 pt 5
	 