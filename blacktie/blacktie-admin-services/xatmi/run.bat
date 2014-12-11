@echo off

echo "Quickstart: Running XATMI admin quickstart"

rem RUN THE FOOAPP SERVER
cd ..\..\xatmi\fooapp
call generate_server -Dservice.names=FOOAPP -Dserver.includes=BarService.c -Dserver.name=fooapp
IF %ERRORLEVEL% NEQ 0 exit -1
set BLACKTIE_CONFIGURATION=win32
call btadmin startup
IF %ERRORLEVEL% NEQ 0 echo "Could not start server" & exit -1
set BLACKTIE_CONFIGURATION=

rem RUN THE ADMIN JMX CLIENT
cd ..\..\blacktie-admin-services\xatmi
call generate_client -Dclient.includes=client.c
(echo 0& echo 0& echo 0& echo 0& echo 1) | client
IF %ERRORLEVEL% NEQ 0 exit -1
(echo 0& echo 0& echo 0& echo 0& echo 2) | client
IF %ERRORLEVEL% NEQ 0 exit -1

rem PICK UP THE CLOSING SERVER
@ping 127.0.0.1 -n 3 -w 1000 > nul
