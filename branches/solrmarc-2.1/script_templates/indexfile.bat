@echo off
:: indexfile.bat
:: Import a single marc file into a Solr index
:: # $Id: indexfile.bat
setlocal
set solrjardef=@SOLR_JAR_DEF@
::Get the current batch file's short path
for %%x in (%0) do set scriptdir=%%~dpsx
for %%x in (%scriptdir%) do set scriptdir=%%~dpsx
::echo BatchPath = %scriptdir%
::
if "%SOLRMARC_MEM_ARGS%" EQU ""  set SOLRMARC_MEM_ARGS=@MEM_ARGS@
::
java %SOLRMARC_MEM_ARGS% %solrjardef% -jar %scriptdir%@CUSTOM_JAR_NAME@ %1 %2 %3
