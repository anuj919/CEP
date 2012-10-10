all: plots show

show:
	evince *.ps

1: plots
	mkdir OneState
	./addTime.sh result.csv
	./update_stat.sh ./stat.txt
	mv *.ps OneState/.
	mv *.csv OneState/.
	mv *.txt OneState/.
#	evince OneState/*.ps

2: plots
	mkdir AutomatonWithReuse
	./addTime.sh result.csv
	./update_stat.sh ./stat.txt
	mv *.ps AutomatonWithReuse/.
	mv *.csv AutomatonWithReuse/.
	mv *.txt AutomatonWithReuse/.
#	evince AutomatonWithReuse/*.ps

3: plots
	mkdir AutomatonWithNoReuse
	./addTime.sh result.csv
	./update_stat.sh ./stat.txt
	mv *.ps AutomatonWithNoReuse/.
	mv *.csv AutomatonWithNoReuse/.
	mv *.txt AutomatonWithNoReuse/.
#	evince AutomatonWithNoReuse/*.ps

plots: plot-memory.ps plot-cpu.ps

plot-memory.ps: result.csv plot-memory.p
	gnuplot plot-memory.p

plot-cpu.ps: result.csv plot-cpu.p
	gnuplot plot-cpu.p

clean:
	rm plot-cpu.ps plot-memory.ps
	rm -ir OneState AutomatonWith* 2>/dev/null
