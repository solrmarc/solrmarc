@echo off

setlocal

::Get the current batch file's short path
for %%x in (%0) do set scriptdir=%%~dpsx
for %%x in (%scriptdir%) do set scriptdir=%%~dpsx

set solrpath=
set url=
set jar=
set config=

if "%1" NEQ "" call :set_arg %1
if "%2" NEQ "" call :set_arg %2
if "%3" NEQ "" call :set_arg %3
if "%4" NEQ "" call :set_arg %4

::echo jar = %jar%
::echo config = %config%
::echo solrpath = %solrpath%
::echo url = %url%


if "%jar%" == ""  set jar=%scriptdir%@CUSTOM_JAR_NAME@

set defconfig=

java -Done-jar.main.class="org.solrmarc.tools.GetDefaultConfig" -jar "%jar%" > %tmp%\_tmpdefconfig

set /p defconfig= < %tmp%\_tmpdefconfig

del /q %tmp%\_tmpdefconfig

if "%config%" == "" if %jar% NEQ "" @set config=%defconfig%

pushd %tmp%
jar xf %jar% %config% 
popd

if "%solrpath%" NEQ "" goto :hiturl 
if "%url%" NEQ "" goto :hiturl
    for /f "usebackq delims=" %%a in (`type %tmp%\%config% ^| findstr "^solr\.path" `) do set solrpathline=%%a
    for /f "usebackq delims=" %%g in (`type %tmp%\%config% ^| findstr "^solr\.hosturl"`) do set urlline=%%g
    echo jar = %jar%
    echo config = %config%
    echo solrpath = %solrpathline:~12%
    echo url = %urlline:~15%
    del "%tmp%\%config%"
    goto :done


::echo jar = %jar%
::echo config = %config%
::echo solrpath = %solrpath%
::echo url = %url%

:hiturl

if "%solrpath%" NEQ "" goto :dont_hit_url
if "%url%" == "" goto :dont_hit_url

    echo no solrpath, hitting URL

    java -Done-jar.main.class="org.solrmarc.tools.GetSolrHomeFromServer" -jar %jar% %url% > %tmp%/_solrhome
    
	set /p solrpath= < %tmp%/_solrhome
    
    del /q %tmp%\_solrhome
    
    if "%solrpath%" == "" (
        echo "Error: no Solr server currently running at URL: %url%"
        goto :done
    )


:dont_hit_url

	if NOT EXIST %solrpath% (
	
	    echo "Error: unable to access directory designated as Solr home: %solrpath%"
        goto :done
	)

	if NOT EXIST %solrpath%/conf (
	    echo "Error: no 'conf' directory in directory designated as Solr home: %solrpath%"
        goto :done
	)

echo jar = %jar%
echo config = %config%
echo solrpath = %solrpath%
echo url = %url%/update

set solrpathdef=solr.path=%solrpath%
set urldef=
if "%url%" NEQ "" set urldef=solr.hosturl=%url%/update

java -Done-jar.main.class="org.solrmarc.tools.ConfigDisplayer" -jar %jar% %config% "%solrpathdef%" "%urldef%" > %tmp%\%config%

pushd %tmp%

type %tmp%\%config%

jar uf %jar% %config%

del /q %config%

popd

GOTO :done


:output_line 

set _line1=%1
set _line2=%_line1:~1%
set _line=%_line2:~0,-1%
set _configtmp=%2
set _solrpath=%3
set _url=%4

set _line9=%_line:~0,9%
set _line12=%_line:~0,12%

if "%_line9%" NEQ "solr.path" if "%_line12%" NEQ "solr.hosturl" echo %_line% >> %_configtmp%
if "%_line12%" == "solr.hosturl" if "%_url%" == "" echo %_line% >> %_configtmp%
if "%_line12%" == "solr.hosturl" if "%_url%" NEQ "" echo solr.hosturl = %_url% >> %_configtmp%
if "%_line9%" == "solr.path"  echo solr.path = %_solrpath% >> %_configtmp%

goto :eof

:set_arg

set arg=%1
if "%arg:~0,4%" == "http" set url=%arg%
if "%arg:~-17%" == "config.properties" set config=%arg%
if "%arg:~-4%" == ".jar" set jar=%arg%
if "%arg:~-4%" NEQ ".jar" if "%arg:~1,2%" == ":/" set solrpath=%arg%
if "%arg:~-4%" NEQ ".jar" if "%arg:~1,2%" == ":\" set solrpath=%arg%
if "%arg:~-4%" NEQ ".jar" if "%arg:~0,1%" == "." for %%g in (%arg%) do set solrpath=%%~fg

goto :eof


endlocal
:done