@echo off
:: getfromsolr.bat
:: Program to extract one or more MARC records from a solr index 
:: $Id: getfromsolr.bat 
::Get the current batch file's short path
set solrjardef=@SOLR_JAR_DEF@
::
for %%x in (%0) do set scriptdir=%%~dpsx
for %%x in (%scriptdir%) do set scriptdir=%%~dpsx
::
if (%1) EQU () goto usage
::
java @MEM_ARGS@ %solrjardef% -Done-jar.main.class=org.solrmarc.marc.SolrReIndexer -jar %scriptdir%@CUSTOM_JAR_NAME@ %1 %2 %3 2> NUL
::
goto done
::
:usage
echo Usage: %0 field:term (field_name_containing_marc_record) 
:done
