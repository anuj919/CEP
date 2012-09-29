all: plots

1: plots
	mkdir OneState
	mv *.ps OneState/.
	cp *.csv OneState/.
	evince OneState/*.ps

2: plots
	mkdir AutomatonWithReuse
	mv *.ps AutomatonWithReuse/.
	cp *.csv AutomatonWithReuse/.
	evince AutomatonWithReuse/*.ps

3: plots
	mkdir AutomatonWithNoReuse
	mv *.ps AutomatonWithNoReuse/.
	cp *.csv AutomatonWithNoReuse/.
	evince AutomatonWithNoReuse/*.ps

plots: plot-memory.ps plot-cpu.ps

plot-memory.ps: result.csv plot-memory.p
	gnuplot plot-memory.p

plot-cpu.ps: result.csv plot-cpu.p
	gnuplot plot-cpu.p

clean:
	rm plot-cpu.ps plot-memory.ps
	rm -ir OneState AutomatonWith* 2>/dev/null
