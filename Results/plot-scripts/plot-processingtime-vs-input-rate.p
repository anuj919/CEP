set term postscript enhanced color

set xlabel "Event Rate (events/s)"
set ylabel "Processing Time (us/event)"

set output 'plot-processingtime-vs-input-rate.ps'

plot '< grep "OneState" results.txt' using 2:9 title 'OneState' with lp, \
   '< grep "AutomatonWithReuse" results.txt' using 2:9 title 'AutomatonWithReuse' with lp, \
      '< grep "AutomatonWithNoReuse" results.txt' using 2:9 title 'AutomatonWithNoReuse' with lp
	 