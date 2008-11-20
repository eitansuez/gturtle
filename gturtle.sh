#!/bin/sh

java -classpath /home/eitan/work/thirdparty/groovy/embeddable/groovy-all-1.6-beta-2.jar:lib/jsyntaxpane-0.9.3.jar:out gturtle.TurtleConsole

# alternative:
#java -classpath dist/GTurtle.jar:lib/jsyntaxpane-0.9.3.jar:/home/eitan/work/thirdparty/groovy/embeddable/groovy-all-1.6-beta-2.jar gturtle.TurtleConsole
# also can use "ant run"
# make sure groovy_home is set

