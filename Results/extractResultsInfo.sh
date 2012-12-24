#!/bin/bash

if [[ $# != 1 ]]
then
    echo "Usage: $0 <dir with Nclasses results>"
    exit 1
fi

cd $1

for classesdir in $(ls | grep classes | sort -n)
do
    numClasses=$(echo $classesdir | sed 's/[^0-9]//g')
    pushd $classesdir >/dev/null
    rm results.txt

#    cd $classesdir
    for testcasedir in $(ls | grep testcase | sort -k 1.9 -n )
    do
	testnumber=$(echo $testcasedir | sed 's/[^0-9]//g')
	rate=$(head -1 $testcasedir/OneState/sender.txt | awk '{print $1}')
	duration=$(head -1 $testcasedir/OneState/client.txt | awk '{print $NF}')
	line_prefix=$testnumber" "$rate" "$duration" "
	
	onestate_avg_memory=$(tail -n +2 $testcasedir/OneState/result.csv | awk 'function abs(x){return ((x < 0.0) ? -x : x)} 
BEGIN{total_usage=0; count=0;prev_usage=0;}
{
  total_usage+=($NF-count)*(prev_usage+abs(prev_usage-$2)/2); 
  count=$NF; prev_usage=$2;
} 
END{print total_usage/count}')
	automatonWithReuse_avg_memory=$(tail -n +2 $testcasedir/AutomatonWithReuse/result.csv | awk 'function abs(x){return ((x < 0.0) ? -x : x)} 
BEGIN{total_usage=0; count=0;prev_usage=0;}
{
  total_usage+=($NF-count)*(prev_usage+abs(prev_usage-$2)/2); 
  count=$NF; prev_usage=$2;
} 
END{print total_usage/count}')
       automatonWithNoReuse_avg_memory=$(tail -n +2 $testcasedir/AutomatonWithNoReuse/result.csv | awk 'function abs(x){return ((x < 0.0) ? -x : x)} 
BEGIN{total_usage=0; count=0;prev_usage=0;}
{
  total_usage+=($NF-count)*(prev_usage+abs(prev_usage-$2)/2); 
  count=$NF; prev_usage=$2;
} 
END{print total_usage/count}')

       onestate_avg_cpu=$(tail -n +2 $testcasedir/OneState/result.csv | awk 'function abs(x){return ((x < 0.0) ? -x : x)} 
BEGIN{total_usage=0; count=0;prev_usage=0;}
{
  total_usage+=($NF-count)*(prev_usage+abs(prev_usage-$4)/2);
  count=$NF; prev_usage=$4;
} 
END{print total_usage/count}')
       automatonWithReuse_avg_cpu=$(tail -n +2 $testcasedir/AutomatonWithReuse/result.csv | awk 'function abs(x){return ((x < 0.0) ? -x : x)} 
BEGIN{total_usage=0; count=0;prev_usage=0;}
{
  total_usage+=($NF-count)*(prev_usage+abs(prev_usage-$4)/2);
  count=$NF; prev_usage=$4;
} 
END{print total_usage/count}')
       automatonWithNoReuse_avg_cpu=$(tail -n +2 $testcasedir/AutomatonWithNoReuse/result.csv | awk 'function abs(x){return ((x < 0.0) ? -x : x)} 
BEGIN{total_usage=0; count=0;prev_usage=0;}
{
  total_usage+=($NF-count)*(prev_usage+abs(prev_usage-$4)/2);
  count=$NF; prev_usage=$4;
} 
END{print total_usage/count}')


       # process for drop rate
       processed=$(head -1 $testcasedir/OneState/stat.txt | awk '{print $NF}')
       dropped=$(head -2 $testcasedir/OneState/stat.txt | tail -1 | awk '{print $NF}')
       onestate_drop_perc=$(echo "scale=2; $dropped*100/($dropped+$processed)" | bc -l)
       
       processed=$(head -1 $testcasedir/AutomatonWithReuse/stat.txt | awk '{print $NF}')
       dropped=$(head -2 $testcasedir/AutomatonWithReuse/stat.txt | tail -1 | awk '{print $NF}')
       automatonWithReuse_drop_perc=$(echo "scale=2; $dropped*100/($dropped+$processed)" | bc -l)     
       
       processed=$(head -1 $testcasedir/AutomatonWithNoReuse/stat.txt | awk '{print $NF}')
       dropped=$(head -2 $testcasedir/AutomatonWithNoReuse/stat.txt | tail -1 | awk '{print $NF}')
       automatonWithNoReuse_drop_perc=$(echo "scale=2; $dropped*100/($dropped+$processed)" | bc -l)     

       # processing time
       onestate_proc_time=$(tail -1 $testcasedir/OneState/stat.txt | awk '{print $NF}')
       automatonWithReuse_proc_time=$(tail -1 $testcasedir/AutomatonWithReuse/stat.txt | awk '{print $NF}')
       automatonWithNoReuse_proc_time=$(tail -1 $testcasedir/AutomatonWithNoReuse/stat.txt | awk '{print $NF}')

       echo $line_prefix"OneState "$onestate_avg_memory" "$onestate_avg_cpu" "$onestate_drop_perc" "$onestate_proc_time >> results.txt
       echo $line_prefix"AutomatonWithReuse "$automatonWithReuse_avg_memory" "$automatonWithReuse_avg_cpu" "$automatonWithReuse_drop_perc" "$automatonWithReuse_proc_time >> results.txt
       echo $line_prefix"AutomatonWithNoReuse "$automatonWithNoReuse_avg_memory" "$automatonWithNoReuse_avg_cpu" "$automatonWithNoReuse_drop_perc" "$automatonWithNoReuse_proc_time >> results.txt
    done 2>/dev/null
    popd >/dev/null
done
