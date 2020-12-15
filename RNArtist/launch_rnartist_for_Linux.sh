#!/bin/bash
#set here your the location of your Java platform
JAVA_HOME="/Library/Java/JavaVirtualMachines/jdk-14.jdk/Contents/Home"

cd "`dirname "$0"`"
$JAVA_HOME/bin/java -Xms500M -Xmx500M --module-path lib --add-modules=javafx.controls,javafx.swing,javafx.web --add-exports javafx.graphics/com.sun.javafx.css=ALL-UNNAMED --add-opens javafx.controls/javafx.scene.control.skin=ALL-UNNAMED -jar lib/rnartist-1.0.jar