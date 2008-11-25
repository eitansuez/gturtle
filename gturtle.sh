#!/bin/sh

# make sure groovy_home is set

cwd=`dirname "$0"`
groovyalljar=groovy-all-1.5.7.jar

java -classpath $GROOVY_HOME/embeddable/$groovyalljar:$cwd/lib/jsyntaxpane-0.9.3.jar:$cwd/out gturtle.TurtleConsole
# alternative:
#java -classpath $cwd/dist/GTurtle.jar:$cwd/lib/jsyntaxpane-0.9.3.jar:$GROOVY_HOME/embeddable/$groovyalljar gturtle.TurtleConsole

# also can use "ant run"

