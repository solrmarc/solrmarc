@echo off
:: getrecord.bat
:: Program to extract one or more MARC records from a file 
:: $Id: getrecord.bat 

::Get the current batch file's short path
for %%x in (%0) do set scriptdir=%%~dpsx
for %%x in (%scriptdir%) do set scriptdir=%%~dpsx
set inarg=%1

set arg1=-
for /f "delims=" %%a in ('echo %inarg% ^| findstr "\.mrc"') do @set arg1=%%a

if "%arg1%" EQU "-" set arg2=%1
if "%arg1%" NEQ "-" set arg2=%2

if "%arg1%" EQU "-" set arg3=%2 
if "%arg1%" NEQ "-" set arg3=%3

java @MEM_ARGS@ -Done-jar.main.class="org.solrmarc.marc.RawRecordReader" -jar %scriptdir%@CUSTOM_JAR_NAME@ %arg1% %arg2% %arg3%
