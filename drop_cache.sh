#!/bin/bash
passwd="test"
echo $passwd | sudo -S bash -c "echo 1 > /proc/sys/vm/drop_caches" >/dev/null
echo $passwd | sudo -S bash -c "echo 2 > /proc/sys/vm/drop_caches" >/dev/null
echo $passwd | sudo -S bash -c "echo 3 > /proc/sys/vm/drop_caches" >/dev/null

