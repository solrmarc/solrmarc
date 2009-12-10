@echo off
:: printrecord.bat
:: Diagnostic program to display marc records.
:: $Id: printrecord.bat
setlocal
::Get the current batch file's short path
for %%x in (%0) do set scriptdir=%%~dpsx
for %%x in (%scriptdir%) do set scriptdir=%%~dpsx
::echo BatchPath = %scriptdir%

java -Dsolrmarc.main.class="org.solrmarc.marc.MarcPrinter" -jar %scriptdir%SolrMarc.jar print %1 %2 %3
