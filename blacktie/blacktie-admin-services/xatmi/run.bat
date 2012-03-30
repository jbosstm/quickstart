@echo off

echo "Quickstart: Running XATMI admin quickstart"

rem RUN THE FOOAPP SERVER
cd %BLACKTIE_HOME%\quickstarts\xatmi\fooapp
call generate_server -Dservice.names=FOOAPP -Dserver.includes=BarService.c -Dserver.name=fooapp
IF %ERRORLEVEL% NEQ 0 exit -1
set BLACKTIE_CONFIGURATION=win32
call btadmin startup
IF %ERRORLEVEL% NEQ 0 exit -1
set BLACKTIE_CONFIGURATION=

rem RUN THE ADMIN JMX CLIENT
cd %BLACKTIE_HOME%\quickstarts\admin\xatmi
call generate_client -Dclient.includes=client.c
(echo 0& echo 0& echo 0& echo 0& echo 1) | client
IF %ERRORLEVEL% NEQ 0 exit -1
cd %BLACKTIE_HOME%\quickstarts\admin\xatmi
(echo 0& echo 0& echo 0& echo 0& echo 2) | client
IF %ERRORLEVEL% NEQ 0 exit -1

rem PICK UP THE CLOSING SERVER
@ping 127.0.0.1 -n 3 -w 1000 > nul
