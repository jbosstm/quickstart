@echo off

echo "Quickstart: Running nbf quickstart "

rem RUN NBF EXAMPLE 
call generate_server -Dservice.names=NBFEXAMPLE -Dserver.includes=NBFService.c -Dserver.name=nbfserv
IF %ERRORLEVEL% NEQ 0 exit -1
set BLACKTIE_CONFIGURATION=win32
call btadmin startup
IF %ERRORLEVEL% NEQ 0 exit -1
set BLACKTIE_CONFIGURATION=

rem RUN THE C CLIENT
call generate_client -Dclient.includes=client.c
client
IF %ERRORLEVEL% NEQ 0 exit -1

rem SHUTDOWN THE SERVER RUNNING THE btadmin TOOL
set BLACKTIE_CONFIGURATION=win32
call btadmin shutdown
IF %ERRORLEVEL% NEQ 0 exit -1
set BLACKTIE_CONFIGURATION=
