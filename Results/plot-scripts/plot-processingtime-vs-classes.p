set term postscript enhanced color
set output "plot-processingtime-vs-classes.ps"
set title "Processing-time vs #Classes \n \
Input event-rate=100 events/s, Duration=50, Predicate selectivity=0.08"
set ylabel "Processing-time (us) (logscale)"
set xlabel "#Classes"
set xrange [2:10]
set logscale y

system "egrep '^[0-9]+ 100 ' 3classes/results.txt | awk '{print $4 \" \" $5 \" \" $NF}' >/tmp/plot-data.txt"
system "egrep '^[0-9]+ 100 ' 5classes/results.txt | awk '{print $4 \" \" $5 \" \" $NF}' >>/tmp/plot-data.txt"
system "egrep '^[0-9]+ 100 ' 7classes/results.txt | awk '{print $4 \" \" $5 \" \" $NF}' >>/tmp/plot-data.txt"
system "egrep '^[0-9]+ 100 ' 9classes/results.txt | awk '{print $4 \" \" $5 \" \" $26}' >>/tmp/plot-data.txt"

plot '< grep OneState /tmp/plot-data.txt' using ($2-0.2):3:(0.2) title "OneState" with boxes fs solid 0.7 noborder,\
   '< grep AutomatonWithReuse /tmp/plot-data.txt' using 2:3:(0.2) title "AutomatonWithReuse" with boxes fs solid 0.5 noborder, \
         '< grep AutomatonWithNoReuse /tmp/plot-data.txt' using ($2+0.2):3:(0.2) title "AutomatonWithNoReuse" with boxes fs solid 0.5 noborder
	    