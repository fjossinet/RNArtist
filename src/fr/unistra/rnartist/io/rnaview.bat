@echo off

Rem Usage from Windows CMD or PowerShell: rnaview.bat C:\Users\Admin\Downloads\ur0012.pdb

Rem first the location of RNAVIEW in Ubuntu WSL (described using Linux path)
set RNAVIEW=/mnt/c/tools/RNAVIEW
set WSLENV=RNAVIEW/u

Rem then the location of the PDB file provided as script argument (described using Windows path)
set PDB_FILE=%1
set WSLENV=%WSLENV%:PDB_FILE/p

bash -c "$RNAVIEW/bin/rnaview -p $PDB_FILE"

