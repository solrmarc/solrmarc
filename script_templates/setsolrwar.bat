echo off

setlocal
set file_=%1
echo file = %file_%

::Get the current batch file's short path
for %%x in (%0) do set scriptdir=%%~dpsx
for %%x in (%scriptdir%) do set scriptdir=%%~dpsx

if "%file_%"=="" goto _usage

dir /-p %file_% 2> nul |find " Directory of "|find /i "%file_%">nul
set found_=yes
if errorlevel==2 set found_=no
if errorlevel==1 set found_=no

if "%found_%"=="yes" goto _directory
goto _file

:_directory

dir /-p %file_%\*.jar 2> nul | find /i "jar" > nul
set found_=yes
if errorlevel==2 set found_=no
if errorlevel==1 set found_=no

if "%found_%"=="yes" goto _checkdir
echo Error : No Jar files found in specified solr jar directory:   
echo Error :   %file_%
echo.
goto _done

:_checkdir
dir /-p %file_%\apache-solr*.jar 2> nul | find /i "jar" > nul
set found_=yes
if errorlevel==2 set found_=no
if errorlevel==1 set found_=no

if "%found_%"=="yes" goto _process_all
echo Error : No solr jar files found in specified solr jar directory:  
echo Error :   %file_%
echo.
goto _done

:_file
set suffix_=%file_:~-4%
echo %suffix_%
if "%suffix_%"==".war" goto _checkwar1

echo Error : Filename specified is not a .war file:  
echo Error :   %file_%
echo.
goto _done

:_checkwar1
if EXIST %1 goto _checkwar2

echo Error : Specified .war file does not a exist:  
echo Error :   %file_%
echo.
goto _done

:_checkwar2
jar tvf %1 | find "apache-solr" > nul
set found_=yes
if errorlevel==2 set found_=no
if errorlevel==1 set found_=no

if "%found_%"=="yes" goto _process_all

echo Error : Specified .war file does not contain any apache solr jar files: 
echo Error :   %file_%
echo.
goto _done

:_process_all

endlocal 
set SOLRWARLOCATIONORJARDIR=%1
echo set SOLRWARLOCATIONORJARDIR=%1

goto :eof

:_usage
echo   Usage: %0 /full/path/to/solr.war 
echo     or : %0 /full/path/to/directory/with/solr/jars 
echo.

:_done
endlocal

