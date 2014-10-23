SRC = \
CoordCube.java \
CubieCube.java \
MainProgram.java \
Search.java \
Util.java \
Tools.java

nprobe = 0;
ifdef probe
	nprobe = $(probe)
endif

DIST = twophase.jar

.PHONY: build clean run testRnd testSel

build: $(DIST)

$(DIST): $(SRC)
	@javac -d . $(SRC)
	@cp -f $(SRC) cs/min2phase/
	@jar cfe twophase.jar ui.MainProgram ui/*.class cs/min2phase/*.class cs/min2phase/*.java

run: build
	@java -jar twophase.jar

testRnd: test.class
	@java -cp .:twophase.jar test 40 1000 21 100000 $(nprobe) 0

testSel: test.class
	@java -cp .:twophase.jar test 24

test.class: $(DIST) test.java
	@javac -d . -cp twophase.jar test.java

rebuild: clean build

clean:
	@rm $(DIST)
	@rm -rf cs ui *.class
