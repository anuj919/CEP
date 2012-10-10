set term postscript enhanced color
set output "plot-supported-rate-vs-classes.ps"
set title "Input-rate vs #Classes \n \
Drop perc. threshold=10%, Duration=50, Predicate selectivity=0.08"
set ylabel "Input Rate (events/s)"
set xlabel "#Classes"
set xrange [2:10]
#set logscale y

system "echo 3 $(cat 3classes/results.txt | grep OneState | awk 'BEGIN{rate=0;prev=0;prev_rate=0}{if($(NF-2)==10) {rate=$2;exit} if ($(NF-2)<10) {rate=$2;prev=$(NF-2);prev_rate=$2} if($(NF-2)>10 && prev<10) {rate=($2-prev_rate)/($(NF-2)-prev)*10+prev_rate;prev=$2;prev_rate=$2}}END{print rate}') >/tmp/plot-data.txt"

system "echo 5 $(cat 5classes/results.txt | grep OneState | awk 'BEGIN{rate=0;prev=0;prev_rate=0}{if($(NF-2)==10) {rate=$2;exit} if ($(NF-2)<10) {rate=$2;prev=$(NF-2);prev_rate=$2} if($(NF-2)>10 && prev<10) {rate=($2-prev_rate)/($(NF-2)-prev)*10+prev_rate;prev=$2;prev_rate=$2}}END{print rate}') >>/tmp/plot-data.txt"

system "echo 7 $(cat 7classes/results.txt | grep OneState | awk 'BEGIN{rate=0;prev=0;prev_rate=0}{if($(NF-2)==10) {rate=$2;exit} if ($(NF-2)<10) {rate=$2;prev=$(NF-2);prev_rate=$2} if($(NF-2)>10 && prev<10) {rate=($2-prev_rate)/($(NF-2)-prev)*10+prev_rate;prev=$2;prev_rate=$2}}END{print rate}') >>/tmp/plot-data.txt"

system "echo 9 $(cat 9classes/results.txt | grep OneState | awk 'BEGIN{rate=0;prev=0;prev_rate=0}{if($(NF-2)==10) {rate=$2;exit} if ($(NF-2)<10) {rate=$2;prev=$(NF-2);prev_rate=$2} if($(NF-2)>10 && prev<10) {rate=($2-prev_rate)/($(NF-2)-prev)*10+prev_rate;prev=$2;prev_rate=$2}}END{print rate}') >>/tmp/plot-data.txt"


system "echo 3 $(cat 3classes/results.txt | grep AutomatonWithReuse | awk 'BEGIN{rate=0;prev=0;prev_rate=0}{if($(NF-2)==10) {rate=$2;exit} if ($(NF-2)<10) {rate=$2;prev=$(NF-2);prev_rate=$2} if($(NF-2)>10 && prev<10) {rate=($2-prev_rate)/($(NF-2)-prev)*10+prev_rate;prev=$2;prev_rate=$2}}END{print rate}') >/tmp/plot-data1.txt"

system "echo 5 $(cat 5classes/results.txt | grep AutomatonWithReuse | awk 'BEGIN{rate=0;prev=0;prev_rate=0}{if($(NF-2)==10) {rate=$2;exit} if ($(NF-2)<10) {rate=$2;prev=$(NF-2);prev_rate=$2} if($(NF-2)>10 && prev<10) {rate=($2-prev_rate)/($(NF-2)-prev)*10+prev_rate;prev=$2;prev_rate=$2}}END{print rate}') >>/tmp/plot-data1.txt"

system "echo 7 $(cat 7classes/results.txt | grep AutomatonWithReuse | awk 'BEGIN{rate=0;prev=0;prev_rate=0}{if($(NF-2)==10) {rate=$2;exit} if ($(NF-2)<10) {rate=$2;prev=$(NF-2);prev_rate=$2} if($(NF-2)>10 && prev<10) {rate=($2-prev_rate)/($(NF-2)-prev)*10+prev_rate;prev=$2;prev_rate=$2}}END{print rate}') >>/tmp/plot-data1.txt"

system "echo 9 $(cat 9classes/results.txt | grep AutomatonWithReuse | awk 'BEGIN{rate=0;prev=0;prev_rate=0}{if($(NF-2)==10) {rate=$2;exit} if ($(NF-2)<10) {rate=$2;prev=$(NF-2);prev_rate=$2} if($(NF-2)>10 && prev<10) {rate=($2-prev_rate)/($(NF-2)-prev)*10+prev_rate;prev=$2;prev_rate=$2}}END{print rate}') >>/tmp/plot-data1.txt"


system "echo 3 $(cat 3classes/results.txt | grep AutomatonWithNoReuse | awk 'BEGIN{rate=0;prev=0;prev_rate=0}{if($(NF-2)==10) {rate=$2;exit} if ($(NF-2)<10) {rate=$2;prev=$(NF-2);prev_rate=$2} if($(NF-2)>10 && prev<10) {rate=($2-prev_rate)/($(NF-2)-prev)*10+prev_rate;prev=$2;prev_rate=$2}}END{print rate}') >/tmp/plot-data2.txt"

system "echo 5 $(cat 5classes/results.txt | grep AutomatonWithNoReuse | awk 'BEGIN{rate=0;prev=0;prev_rate=0}{if($(NF-2)==10) {rate=$2;exit} if ($(NF-2)<10) {rate=$2;prev=$(NF-2);prev_rate=$2} if($(NF-2)>10 && prev<10) {rate=($2-prev_rate)/($(NF-2)-prev)*10+prev_rate;prev=$2;prev_rate=$2}}END{print rate}') >>/tmp/plot-data2.txt"

system "echo 7 $(cat 7classes/results.txt | grep AutomatonWithNoReuse | awk 'BEGIN{rate=0;prev=0;prev_rate=0}{if($(NF-2)==10) {rate=$2;exit} if ($(NF-2)<10) {rate=$2;prev=$(NF-2);prev_rate=$2} if($(NF-2)>10 && prev<10) {rate=($2-prev_rate)/($(NF-2)-prev)*10+prev_rate;prev=$2;prev_rate=$2}}END{print rate}') >>/tmp/plot-data2.txt"

system "echo 9 $(cat 9classes/results.txt | grep AutomatonWithNoReuse | awk 'BEGIN{rate=0;prev=0;prev_rate=0}{if($(NF-2)==10) {rate=$2;exit} if ($(NF-2)<10) {rate=$2;prev=$(NF-2);prev_rate=$2} if($(NF-2)>10 && prev<10) {rate=($2-prev_rate)/($(NF-2)-prev)*10+prev_rate;prev=$2;prev_rate=$2}}END{print rate}') >>/tmp/plot-data2.txt"


plot '/tmp/plot-data.txt' using ($1-0.2):2:(0.2) title "OneState" with boxes fs solid 0.7 noborder,\
   '/tmp/plot-data1.txt' using 1:2:(0.2) title "AutomatonWithReuse" with boxes fs solid 0.5 noborder, \
         '/tmp/plot-data2.txt' using ($1+0.2):2:(0.2) title "AutomatonWithNoReuse" with boxes fs solid 0.5 noborder
	    