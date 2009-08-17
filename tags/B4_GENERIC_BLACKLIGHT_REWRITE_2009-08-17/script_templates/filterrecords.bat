@echo off
:: filterrecords.bat
:: Grep for marc records
:: $Id: filterrecords.bat
setlocal
::Get the current batch file's short path
for %%x in (%0) do set scriptdir=%%~dpsx
for %%x in (%scriptdir%) do set scriptdir=%%~dpsx
::echo BatchPath = %scriptdir%

java -Done-jar.main.class="org.solrmarc.marc.MarcPrinter" -Dmarc.include_if_present="%1" -jar %scriptdir%@CUSTOM_JAR_NAME@ translate %2 %3 
