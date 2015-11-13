SRC = \
CoordCube.java \
CubieCube.java \
MainProgram.java \
Search.java \
Util.java \
Tools.java

ifndef probe
	probe = 0
endif

ifndef maxl
	maxl = 30
endif

ifndef ntest
	ntest = 1000
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
	@java -ea -cp .:twophase.jar test 40 $(ntest) $(maxl) 10000000 $(probe) 0

testSel: test.class
	@java -ea -cp .:twophase.jar test 24

test.class: $(DIST) test.java
	@javac -d . -cp twophase.jar test.java

rebuild: clean build

clean:
	@rm $(DIST)
	@rm -rf cs ui *.class
