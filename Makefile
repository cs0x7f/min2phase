SRC = \
CoordCube.java \
CubieCube.java \
MainProgram.java \
Search.java \
Util.java \
Tools.java

DIST = twophase.jar

.PHONY: build clean run test

build: $(DIST)

twophase.jar: $(SRC)
	@javac -d . $(SRC)
	@cp -f $(SRC) cs/min2phase/
	@jar cfe twophase.jar ui.MainProgram ui/*.class cs/min2phase/*.class cs/min2phase/*.java

run: build
	@java -jar twophase.jar

test: build
	javac -d . -cp twophase.jar test.java
	java -cp .:twophase.jar test 56

rebuild: clean build

clean:
	@rm $(DIST)
	@rm -rf cs ui *.class
