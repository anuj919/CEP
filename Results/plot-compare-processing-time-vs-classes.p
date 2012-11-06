set term postscript enhanced color
set output "plot-compare-processing-time-vs-classes.ps"
set title "Processing-time vs #Classes \n \
Input rate=100 events/s, Duration=50, Predicate selectivity=0.08"
set ylabel "Processing Time (us) (logscale)"
set xlabel "#Classes"
set xrange [2:10]
set logscale y

system "grep OneState 3classes/results.txt | egrep '^[0-9]+ 100 '>/tmp/plot-data.txt"
system "grep OneState 5classes/results.txt | egrep '^[0-9]+ 100 '>>/tmp/plot-data.txt"
system "grep OneState 7classes/results.txt | egrep '^[0-9]+ 100 '>>/tmp/plot-data.txt"
system "grep OneState 9classes/results.txt | egrep '^[0-9]+ 100 '>>/tmp/plot-data.txt"

system "grep AutomatonWithReuse 3classes/results.txt | egrep '^[0-9]+ 100 '>/tmp/plot-data1.txt"
system "grep AutomatonWithReuse 5classes/results.txt | egrep '^[0-9]+ 100 '>>/tmp/plot-data1.txt"
system "grep AutomatonWithReuse 7classes/results.txt | egrep '^[0-9]+ 100 '>>/tmp/plot-data1.txt"
system "grep AutomatonWithReuse 9classes/results.txt | egrep '^[0-9]+ 100 '>>/tmp/plot-data1.txt"

system "grep AutomatonWithNoReuse 3classes/results.txt | egrep '^[0-9]+ 100 '>/tmp/plot-data2.txt"
system "grep AutomatonWithNoReuse 5classes/results.txt | egrep '^[0-9]+ 100 '>>/tmp/plot-data2.txt"
system "grep AutomatonWithNoReuse 7classes/results.txt | egrep '^[0-9]+ 100 '>>/tmp/plot-data2.txt"
system "grep AutomatonWithNoReuse 9classes/results.txt | egrep '^[0-9]+ 100 '>>/tmp/plot-data2.txt"


plot '< grep OneState /tmp/plot-data.txt' using ($5-0.2):9:(0.2) title "OneState" with boxes fs solid 0.7 noborder,\
   '/tmp/plot-data1.txt' using 5:9:(0.2) title "AutomatonWithReuse" with boxes fs solid 0.5 noborder, \
         '/tmp/plot-data2.txt' using ($5+0.2):9:(0.2) title "AutomatonWithNoReuse" with boxes fs solid 0.5 noborder