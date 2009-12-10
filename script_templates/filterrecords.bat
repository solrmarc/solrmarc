@echo off
:: filterrecords.bat
:: Grep for marc records
:: $Id: filterrecords.bat
setlocal
::Get the current batch file's short path
for %%x in (%0) do set scriptdir=%%~dpsx
for %%x in (%scriptdir%) do set scriptdir=%%~dpsx
::echo BatchPath = %scriptdir%

java -Dsolrmarc.main.class="org.solrmarc.marc.MarcPrinter" -Dmarc.include_if_present="%1" -Dmarc.combine_records="" -jar %scriptdir%SolrMarc.jar translate %2 %3 
