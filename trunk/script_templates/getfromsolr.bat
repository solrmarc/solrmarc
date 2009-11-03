@echo off
:: getfromsolr.bat
:: Program to extract one or more MARC records from a solr index 
:: $Id: getfromsolr.bat 
::Get the current batch file's short path
setlocal
set solrjardef=@SOLR_JAR_DEF@
::
for %%x in (%0) do set scriptdir=%%~dpsx
for %%x in (%scriptdir%) do set scriptdir=%%~dpsx
::
if "%1" EQU "" goto usage
::
set id=
set url=
set query=
::
if "%1" NEQ "" call :set_arg %1
if "%2" NEQ "" call :set_arg %2
::echo id=%id%
::echo url=%url%
::
if "%query%" == "" if "%id%" NEQ "" set query=id:%id%
::echo query=%query%
::
if "%SOLRMARC_MEM_ARGS%" EQU ""  set SOLRMARC_MEM_ARGS=@MEM_ARGS@
::
if "%url%" == "" java %SOLRMARC_MEM_ARGS% %solrjardef% -Done-jar.main.class=org.solrmarc.marc.SolrReIndexer -jar %scriptdir%@CUSTOM_JAR_NAME@ "%query%" 2> NUL
if "%url%" NEQ "" java %SOLRMARC_MEM_ARGS% -Done-jar.main.class="org.solrmarc.solr.RemoteSolrSearcher" -jar %scriptdir%@CUSTOM_JAR_NAME@ %url% "%query%" 
::
goto done
::
:usage
echo Usage: %0 field:term (field_name_containing_marc_record) 
goto done
::
:set_arg
::
set arg=%1
if "%arg:~0,4%" == "http" goto set_url
for /f "tokens=1,2 delims=:" %%g in ("%arg%") do set a1=%%g&set a2=%%h
if "%a2%" == "" set id=%a1%
if "%a2%" NEQ "" set query=%arg%
goto :eof
::
:set_url
set url=%arg%
::
goto :eof
::
:done
