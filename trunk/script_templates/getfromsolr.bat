@echo off
:: getfromsolr.bat
:: Program to extract one or more MARC records from a solr index 
:: $Id: getfromsolr.bat 

::Get the current batch file's short path
for %%x in (%0) do set scriptdir=%%~dpsx
for %%x in (%scriptdir%) do set scriptdir=%%~dpsx

java @MEM_ARGS@ -Done-jar.main.class="org.solrmarc.marc.SolrReIndexer" -jar %scriptdir%@CUSTOM_JAR_NAME@ %1 %2 %3 2> NUL
