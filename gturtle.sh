#!/bin/sh

# make sure groovy_home is set

cwd=`dirname "$0"`
if [ -z "$GROOVY_HOME" ] ; then
  GROOVY_HOME="/usr/local/Cellar/groovy/2.4.3/libexec"
fi

groovyalljar=groovy-all-2.4.3.jar

java -classpath $GROOVY_HOME/embeddable/$groovyalljar:$cwd/lib/jsyntaxpane-0.9.3.jar:$cwd/build/libs/gturtle.jar gturtle.TurtleConsole
# alternative:
#java -classpath $cwd/dist/GTurtle.jar:$cwd/lib/jsyntaxpane-0.9.3.jar:$GROOVY_HOME/embeddable/$groovyalljar gturtle.TurtleConsole

# also can use "ant run"

