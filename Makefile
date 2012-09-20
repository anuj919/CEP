all:


generateData:
	cd bin; pwd; cp ../spec.txt .; \
	java -cp .:$(shell echo ../lib/*.jar | tr ' ' ':') testdatagenerator/GenerateRandomEvents ; \
	rm spec.txt

testConcurrent:
	cd bin; \
	java -cp .:$(echo ../lib/*.jar | tr ' ' ':') testcase/TestConcurrentState
