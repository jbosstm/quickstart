@echo off

echo "Running JAB quickstart"

rem RUN THE FOOAPP SERVER
cd ..\..\xatmi\fooapp
call generate_server -Dservice.names=FOOAPP -Dserver.includes=BarService.c -Dserver.name=fooapp
IF %ERRORLEVEL% NEQ 0 exit -1
set BLACKTIE_CONFIGURATION=win32
call btadmin startup
IF %ERRORLEVEL% NEQ 0 exit -1
set BLACKTIE_CONFIGURATION=

rem RUN THE JAVA CLIENT
cd ..\..\jatmibroker-xatmi\jab
echo hello | mvn test
IF %ERRORLEVEL% NEQ 0 exit -1

cd ..\..\xatmi\fooapp
call btadmin shutdown
IF %ERRORLEVEL% NEQ 0 exit -1
