#This is a powershell cript. To be able to launch it, you should need to type first: Set-ExecutionPolicy Unrestricted

#set here your the location of your Java platform
$JAVA_HOME="C:\Program Files\Java\jdk-13.0.1\"

cd $PSScriptRoot\lib
Start-Process -FilePath $JAVA_HOME\bin\java.exe -NoNewWindow -Wait -ArgumentList @("-Xms500M -Xmx500M --module-path lib --add-modules=javafx.controls,javafx.swing,javafx.web --add-exports javafx.graphics/com.sun.javafx.css=ALL-UNNAMED --add-opens javafx.controls/javafx.scene.control.skin=ALL-UNNAMED -jar lib\rnartist-1.0.jar")
