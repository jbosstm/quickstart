@echo off

echo "Quickstart: Running quickstart to show reply to"

rem RUN THE FOOAPP SERVER
call generate_server -Dserver.includes=BarService.c -Dserver.name=server
IF %ERRORLEVEL% NEQ 0 exit -1
set BLACKTIE_CONFIGURATION=win32
call btadmin startup
set BLACKTIE_CONFIGURATION=
IF %ERRORLEVEL% NEQ 0 exit -1

rem RUN THE C CLIENTA
call generate_client -Dclient.includes=client.c
(echo 0& echo 0) | client clientA CLIENTRESPONSEHANDLER_1 hello1
IF %ERRORLEVEL% NEQ 0 exit -1

rem RUN THE C CLIENTB
(echo 0& echo 0) | client clientB CLIENTRESPONSEHANDLER_2 hello2
IF %ERRORLEVEL% NEQ 0 exit -1

set BLACKTIE_SERVER=server
call btadmin shutdown
set BLACKTIE_SERVER=
IF %ERRORLEVEL% NEQ 0 exit -1
