@echo off

echo "Quickstart: Running library quickstart"

rem GENERATE AN EMPTY SERVER
cd %BLACKTIE_HOME%\quickstarts\xatmi\library
call generate_server -Dserver.name=library
IF %ERRORLEVEL% NEQ 0 exit -1

rem GENERATE A LIBRARY WITH THE BarService SERVICE IN IT
cd %BLACKTIE_HOME%\quickstarts\xatmi\library
call generate_library -Dlibrary.includes=BarService.c
IF %ERRORLEVEL% NEQ 0 exit -1

rem START THE SERVER
cd %BLACKTIE_HOME%\quickstarts\xatmi\library
set BLACKTIE_CONFIGURATION=win32
call btadmin startup
set BLACKTIE_CONFIGURATION=
IF %ERRORLEVEL% NEQ 0 exit -1

rem RUN THE C CLIENT
cd %BLACKTIE_HOME%\quickstarts\xatmi\library
call generate_client -Dclient.includes=client.c
client
IF %ERRORLEVEL% NEQ 0 exit -1

rem SHUTDOWN THE SERVER
cd %BLACKTIE_HOME%\quickstarts\xatmi\library
call btadmin shutdown
IF %ERRORLEVEL% NEQ 0 exit -1
