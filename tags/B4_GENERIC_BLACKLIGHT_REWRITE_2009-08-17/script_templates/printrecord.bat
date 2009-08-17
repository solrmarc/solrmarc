@echo off
:: printrecord.bat
:: Diagnostic program to display marc records.
:: $Id: printrecord.bat
setlocal
::Get the current batch file's short path
for %%x in (%0) do set scriptdir=%%~dpsx
for %%x in (%scriptdir%) do set scriptdir=%%~dpsx
::echo BatchPath = %scriptdir%

java -Done-jar.main.class="org.solrmarc.marc.MarcPrinter" -jar %scriptdir%@CUSTOM_JAR_NAME@ print %1 %2 %3
