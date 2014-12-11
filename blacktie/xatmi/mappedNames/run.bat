@echo off

echo "Running Mapped Service Names"

rem RUN MAPPED SERVICE NAMES EXAMPLE 
call generate_server -Dservice.names=ONE,TWO -Dserver.output.file=hiprio  -Dserver.includes=BarService.c -Dserver.name=hiprio
IF %ERRORLEVEL% NEQ 0 exit -1
call generate_server -Dservice.names=THREE,FOUR -Dserver.output.file=loprio  -Dserver.includes=BarService.c -Dserver.name=loprio
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
