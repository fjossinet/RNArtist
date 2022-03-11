#!/bin/bash

cd "`dirname "$0"`"
java -Xms500M -Xmx500M --module-path lib --add-modules=javafx.controls,javafx.swing -jar lib/rnartist-1.0.jar