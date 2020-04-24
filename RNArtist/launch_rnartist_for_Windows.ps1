#This is a powershell cript. To be able to launch it, you should need to type first: Set-ExecutionPolicy Unrestricted

#set here your the location of your Java platform
$JAVA_HOME="C:\Program Files\Java\jdk-13.0.1\"

cd $PSScriptRoot\lib
Start-Process -FilePath $JAVA_HOME\bin\java.exe -NoNewWindow -Wait -ArgumentList @("-jar rnartist.jar")
