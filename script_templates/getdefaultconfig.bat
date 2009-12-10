@echo off
:: getdefaultconfig.bat
:: Grep for marc records
:: $Id: getdefaultconfig.bat
setlocal
::Get the current batch file's short path
for %%x in (%0) do set scriptdir=%%~dpsx
for %%x in (%scriptdir%) do set scriptdir=%%~dpsx
::echo BatchPath = %scriptdir%

java -Dsolrmarc.main.class="org.solrmarc.tools.GetDefaultConfig" -jar %scriptdir%SolrMarc.jar 
