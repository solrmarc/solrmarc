@echo off
:: indextest.bat
:: Diagnostic program to show how a set of marc records would be indexed,
:: without actually adding any records to Solr.
:: $Id: indextest.bat 

::Get the current batch file's short path
for %%x in (%0) do set scriptdir=%%~dpsx
for %%x in (%scriptdir%) do set scriptdir=%%~dpsx
::echo BatchPath = %scriptdir%

java @MEM_ARGS@ -Done-jar.main.class="org.solrmarc.marc.MarcPrinter" -jar %scriptdir%@CUSTOM_JAR_NAME@ index %1 %2 %3
