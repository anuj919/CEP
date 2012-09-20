parser: testcase/UQLParser.class

testcase/UQLParser.class: testcase/UQLParser.jj
	cd testcase &&	../../../javacc-5.0/bin/javacc UQLParser.jj
	javac -cp ../../lib/javacc.jar":." testcase/UQLParser.java

clean:
	rm testcase/*.class testcase/UQLParser*.java testcase/Token.java testcase/TokenMgrError.java testcase/SimpleCharStream.java testcase/ParseException.java

