#!/bin/sh

cwd=`dirname "$0"`
java -classpath $GROOVY_HOME/embeddable/groovy-all-1.6-beta-2.jar:$cwd/lib/jsyntaxpane-0.9.3.jar:$cwd/out gturtle.TurtleConsole

# alternative:
#java -classpath dist/GTurtle.jar:lib/jsyntaxpane-0.9.3.jar:/home/eitan/work/thirdparty/groovy/embeddable/groovy-all-1.6-beta-2.jar gturtle.TurtleConsole
# also can use "ant run"
# make sure groovy_home is set

