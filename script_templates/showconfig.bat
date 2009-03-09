@echo off


::Get the current batch file's short path
for %%x in (%0) do set scriptdir=%%~dpsx
for %%x in (%scriptdir%) do set scriptdir=%%~dpsx
set jar=
set config=

for /f "delims=" %%a in ('echo %1 ^| egrep "\.jar$"') do @set jar=%%a
for /f "delims=" %%a in ('echo %1 ^| egrep "config\.properties$"') do @set config=%%a
for /f "delims=" %%a in ('echo %2 ^| egrep "\.jar$"') do @set jar=%%a
for /f "delims=" %%a in ('echo %2 ^| egrep "config\.properties$"') do @set config=%%a

if "%jar%" == ""  set jar=%scriptdir%@CUSTOM_JAR_NAME@
set defconfig=

java -Done-jar.main.class="org.solrmarc.tools.GetDefaultConfig" -jar %jar% > %tmp%\tmpdefconfig

set /p defconfig= < %tmp%\tmpdefconfig

del %tmp%\tmpdefconfig

if "%config%" == "" if %jar% NEQ "" @set config=%defconfig%

echo %config%

::tmp=tmp"%random%"

::mkdir %tmp%
pushd %tmp%
jar xf %jar% %config% 

cat "%config%" 

popd ..
del "%tmp%\%config%"

