@echo off
:: updatesolrserver.bat
:: send an update message to a running solr server, 
:: $Id: updatesolrserver.bat

::Get the current batch file's short path
for %%x in (%0) do set scriptdir=%%~dpsx
for %%x in (%scriptdir%) do set scriptdir=%%~dpsx
::echo BatchPath = %scriptdir%

java @MEM_ARGS@ -Done-jar.main.class="org.solrmarc.tools.SolrUpdate" -jar %scriptdir%@CUSTOM_JAR_NAME@ %1
