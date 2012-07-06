@echo off
:: filterrecords.bat
:: Grep for marc records
:: $Id: filterrecords.bat
setlocal
::Get the current batch file's short path
for %%x in (%~f0) do set scriptdir=%%~dpsx
for %%x in (%scriptdir%) do set scriptdir=%%~dpsx

if EXIST %scriptdir%SolrMarc.jar goto doit
pushd %scriptdir%..
for %%x in (%CD%) do set scriptdir=%%~sx\
popd

:doit

java -Dsolrmarc.main.class="org.solrmarc.tools.MergeSummaryHoldings" -jar %scriptdir%SolrMarc.jar %1 %2 %3 %4 %5
goto done

:done