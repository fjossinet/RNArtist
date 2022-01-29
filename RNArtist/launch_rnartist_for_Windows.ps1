#This is a powershell script. To be able to launch it, you should need to type first: Set-ExecutionPolicy Unrestricted

cd $PSScriptRoot
Start-Process -FilePath java.exe -NoNewWindow -Wait -ArgumentList @("-Xms500M -Xmx500M --module-path lib --add-modules=javafx.controls,javafx.swing -jar lib\rnartist-1.0.jar")
