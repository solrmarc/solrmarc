@echo off
:: marcupdate.bat
:: Program to copy a marc record file, merging in updates and deletions
:: $Id:marcupdate.bat
setlocal
::Get the current batch file's short path
for %%x in (%0) do set scriptdir=%%~dpsx
for %%x in (%scriptdir%) do set scriptdir=%%~dpsx


java -Done-jar.main.class="org.solrmarc.marc.MarcMerger" -jar %scriptdir%@CUSTOM_JAR_NAME@ %1 %2 %3