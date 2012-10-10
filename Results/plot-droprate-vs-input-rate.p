set term postscript enhanced color

set ylabel "Drop rate(%)"
set xlabel "Input rate(events/s)"

set output 'plot-droprate-vs-input-rate.ps'

plot '< grep "OneState" results.txt' using 2:8 title 'OneState' with lp, \
   '< grep "AutomatonWithReuse" results.txt' using 2:8 title 'AutomatonWithReuse' with lp, \
      '< grep "AutomatonWithNoReuse" results.txt' using 2:8 title 'AutomatonWithNoReuse' with lp
	 