#!/bin/bash
#set here your the location of your Java platform
JAVA_HOME="/Library/Java/JavaVirtualMachines/jdk-14.jdk/Contents/Home"

cd "`dirname "$0"`"
$JAVA_HOME/bin/java -Xms500M -Xmx500M -jar lib/rnartist.jar