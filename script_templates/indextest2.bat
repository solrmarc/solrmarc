@echo off
:: indextest2.bat
:: Diagnostic program to show how a set of marc records would be indexed,
:: without actually adding any records to Solr.
:: $Id: indextest2.bat 
setlocal
::Get the current batch file's short path
for %%x in (%0) do set scriptdir=%%~dpsx
for %%x in (%scriptdir%) do set scriptdir=%%~dpsx
::echo BatchPath = %scriptdir%
::
if "%SOLRMARC_MEM_ARGS%" EQU ""  set SOLRMARC_MEM_ARGS=@MEM_ARGS@
::
java %SOLRMARC_MEM_ARGS% -Dmarc.just_index_dont_add="true" -jar %scriptdir%SolrMarc.jar %1 %2 %3 


