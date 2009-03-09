@echo off
:: indexfile.bat
:: Import a single marc file into a Solr index
:: # $Id: indexfile.bat

::Get the current batch file's short path
for %%x in (%0) do set scriptdir=%%~dpsx
for %%x in (%scriptdir%) do set scriptdir=%%~dpsx
::echo BatchPath = %scriptdir%

java @MEM_ARGS@ -jar %scriptdir%@CUSTOM_JAR_NAME@ index %1 %2 %3
