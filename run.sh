#!/bin/bash

cd bin
cp ../spec.txt .

java -cp .:$(echo ../../lib/*.jar | tr ' ' ':') testdatagenerator/GenerateRandomEvents
rm spec.txt
