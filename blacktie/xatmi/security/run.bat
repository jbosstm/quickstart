@echo off

echo "Quickstart: Running Security quickstart"

rem RUN THE SECURE SERVER
call generate_server -Dservice.names=SECURE -Dserver.includes=BarService.c -Dserver.name=secure
IF %ERRORLEVEL% NEQ 0 exit -1
set BLACKTIE_CONFIGURATION_DIR=serv
set BLACKTIE_CONFIGURATION=win32
call btadmin startup secure
IF %ERRORLEVEL% NEQ 0 exit -1
set BLACKTIE_CONFIGURATION=
set BLACKTIE_CONFIGURATION_DIR=

rem RUN THE "guest" USER CLIENT
call generate_client -Dclient.includes=client.c
set BLACKTIE_CONFIGURATION_DIR=guest
client
rem This test is expected to fail so make sure the exit status was not 0
IF %ERRORLEVEL% EQU 0 exit -1
set BLACKTIE_CONFIGURATION_DIR=

rem RUN THE "dynsub" USER CLIENT
set BLACKTIE_CONFIGURATION_DIR=dynsub
client
IF %ERRORLEVEL% NEQ 0 exit -1
set BLACKTIE_CONFIGURATION_DIR=

rem SHUTDOWN THE SERVER RUNNING THE btadmin TOOL
set BLACKTIE_CONFIGURATION_DIR=serv
set BLACKTIE_CONFIGURATION=win32
call btadmin shutdown secure
IF %ERRORLEVEL% NEQ 0 exit -1
set BLACKTIE_CONFIGURATION=
set BLACKTIE_CONFIGURATION_DIR=
