@echo off
:: indextest2.bat
:: Diagnostic program to show how a set of marc records would be indexed,
:: without actually adding any records to Solr.
:: $Id: indextest2.bat 
set solrjardef=@SOLR_JAR_DEF@
::Get the current batch file's short path
for %%x in (%0) do set scriptdir=%%~dpsx
for %%x in (%scriptdir%) do set scriptdir=%%~dpsx
::echo BatchPath = %scriptdir%
::
java @MEM_ARGS@ %solrjardef% -Dmarc.just_index_dont_add="true" -jar %scriptdir%@CUSTOM_JAR_NAME@ %1 %2 %3 


