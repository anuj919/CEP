#!/bin/bash

sudo bash -c "echo 1 > /proc/sys/vm/drop_caches"
sudo bash -c "echo 2 > /proc/sys/vm/drop_caches"
sudo bash -c "echo 3 > /proc/sys/vm/drop_caches"

