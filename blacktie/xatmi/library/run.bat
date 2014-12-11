@echo off

echo "Quickstart: Running library quickstart"

rem GENERATE AN EMPTY SERVER
call generate_server -Dserver.name=library
IF %ERRORLEVEL% NEQ 0 exit -1

rem GENERATE A LIBRARY WITH THE BarService SERVICE IN IT
call generate_library -Dlibrary.includes=BarService.c
IF %ERRORLEVEL% NEQ 0 exit -1

rem START THE SERVER
set BLACKTIE_CONFIGURATION=win32
call btadmin startup
set BLACKTIE_CONFIGURATION=
IF %ERRORLEVEL% NEQ 0 exit -1

rem RUN THE C CLIENT
call generate_client -Dclient.includes=client.c
client
IF %ERRORLEVEL% NEQ 0 exit -1

rem SHUTDOWN THE SERVER
call btadmin shutdown
IF %ERRORLEVEL% NEQ 0 exit -1
