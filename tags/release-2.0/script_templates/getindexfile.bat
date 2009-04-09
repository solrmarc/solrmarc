@echo off
::
::Get the current batch file's short path
for %%x in (%0) do set scriptdir=%%~dpsx
for %%x in (%scriptdir%) do set scriptdir=%%~dpsx

set jar=
set properties=

if "%1" NEQ "" call :set_arg %1
if "%2" NEQ "" call :set_arg %2

if "%jar%" EQU "" set jar=%scriptdir%@CUSTOM_JAR_NAME@

if "%properties%" NEQ "" goto haveproperties

java -Done-jar.main.class="org.solrmarc.tools.PropertyFileFetcher" -jar "%jar%" | findstr "solr.indexer.properties" > %tmp%\prop
set /p props=< %tmp%\prop
del /q %tmp%\prop

set properties=%props:~26%

:haveproperties

java -Done-jar.main.class="org.solrmarc.tools.PropertyFileFetcher" -jar "%jar%" %properties% > %properties%

echo Property file: %properties%  extracted from jar file.

goto done

:set_arg

set arg=%1
if "%arg:~-11%" == ".properties" set properties=%arg%
if "%arg:~-4%" == ".jar" set jar=%arg%

goto :eof

:done
endlocal
