@echo off


::Get the current batch file's short path
for %%x in (%0) do set scriptdir=%%~dpsx
for %%x in (%scriptdir%) do set scriptdir=%%~dpsx
set jar=
set config=

if "%1" NEQ "" call :set_arg %1
if "%2" NEQ "" call :set_arg %2

if "%jar%" == ""  set jar=%scriptdir%@CUSTOM_JAR_NAME@
set defconfig=

java -Done-jar.main.class="org.solrmarc.tools.ConfigDisplayer" -jar %jar% %config%

goto :done

:set_arg

set arg=%1
if "%arg:~-17%" == "config.properties" set config=%arg%
if "%arg:~-4%" == ".jar" set jar=%arg%

goto :eof

:done
