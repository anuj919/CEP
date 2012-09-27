all: plots

plots: plot-memory.ps plot-cpu.ps

plot-memory.ps: result.csv plot-memory.p
	gnuplot plot-memory.p

plot-cpu.ps: result.csv plot-cpu.p
	gnuplot plot-cpu.p

clean:
	rm plot-cpu.ps plot-memory.ps
