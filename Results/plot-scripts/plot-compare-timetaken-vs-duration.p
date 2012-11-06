set term postscript enhanced color
set datafile separator ","
set output "plot-compare-timetaken-vs-duration.ps"
set ylabel "Time Taken (s)"
set xlabel "Duration (S)"
#set yrange [0:1024*4.5]
set xtics 10
set xrange [25:75]

#set style data histogram
#set style histogram cluster gap 1
set boxwidth 0.1 relative
set style fill solid 0.8 border -1

plot '< grep -e "4,.*OneState" timing.csv' using ($5-2):($8) title "OneState" with boxes fill pattern 4 8, \
   '< grep -e "6,.*AutomatonWithReuse" timing.csv' using ($5):($8) title "AutomatonWithReuse" with boxes fill pattern 3 8, \
      '< grep -e "9,.*AutomatonWithNoReuse" timing.csv' using ($5+2):($8) title "AutomatonWithNoReuse" with boxes fill pattern 3 9
#plot 'timing.csv' using ($5==50?$8:1/0):xtic(2) title col
#plot 'timing.csv' using ($5==30?$8:1/0):xtic(2) title col


