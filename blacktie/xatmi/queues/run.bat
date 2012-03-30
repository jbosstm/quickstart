@echo off

echo "Quickstart: Running externally managed queue quickstart"

rem Running externally managed queue quickstart
call generate_client -Dclient.includes=queues.c -Dx.define=WIN32
client put 10
IF %ERRORLEVEL% NEQ 0 exit -1
set BLACKTIE_SERVER_ID=1
client get 5
IF %ERRORLEVEL% NEQ 0 exit -1
client get 5
IF %ERRORLEVEL% NEQ 0 exit -1
set BLACKTIE_SERVER_ID=
rem Successful

rem Running txsender queue quickstart
call generate_client -Dclient.includes=txsender.c -Dclient.output.file=txsender -Dx.define=WIN32
IF %ERRORLEVEL% NEQ 0 exit -1
call generate_client -Dclient.includes=queues.c -Dx.define=WIN32
IF %ERRORLEVEL% NEQ 0 exit -1
(echo 1) | txsender
IF %ERRORLEVEL% NEQ 0 exit -1
set BLACKTIE_SERVER_ID=1
client get 2
IF %ERRORLEVEL% NEQ 0 exit -1
set BLACKTIE_SERVER_ID=
rem Successful

rem Running propagated transaction queue quickstart
call generate_client -Dclient.includes=queues.c -Dclient.output.file=client
IF %ERRORLEVEL% NEQ 0 exit -1
call generate_server -Dserver.includes=BarService.c  -Dservice.names=QUEUES -Dserver.name=queues
IF %ERRORLEVEL% NEQ 0 exit -1
call generate_client -Dclient.includes=client.c -Dclient.output.file=clientSender
call btadmin startup
IF %ERRORLEVEL% NEQ 0 exit -1
(echo 1) | clientSender
IF %ERRORLEVEL% NEQ 0 exit -1
set BLACKTIE_SERVER=myserv
set BLACKTIE_SERVER_ID=1
client get 1
IF %ERRORLEVEL% NEQ 0 exit -1
set BLACKTIE_SERVER_ID=
set BLACKTIE_SERVER=
call btadmin shutdown
IF %ERRORLEVEL% NEQ 0 exit -1
rem Successful
