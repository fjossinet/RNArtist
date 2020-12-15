#!/bin/bash

cd "`dirname "$0"`"
java -Xms500M -Xmx500M --module-path lib --add-modules=javafx.controls,javafx.swing,javafx.web --add-exports javafx.graphics/com.sun.javafx.css=ALL-UNNAMED --add-opens javafx.controls/javafx.scene.control.skin=ALL-UNNAMED -jar lib/rnartist-1.0.jar