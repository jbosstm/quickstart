@echo off

echo "Quickstart: Running Topics"

rem CLEAN ALL LOG FILES
del blacktie*.log

rem RUN THE FOOAPP SERVER
call generate_server -Dserver.includes=BarService.c -Dserver.name=myserv
IF %ERRORLEVEL% NEQ 0 exit -1
set BLACKTIE_CONFIGURATION=win32
call btadmin startup
IF %ERRORLEVEL% NEQ 0 exit -1
set BLACKTIE_CONFIGURATION=

rem RUN THE C CLIENT
call generate_client -Dclient.includes=client.c
client
IF %ERRORLEVEL% NEQ 0 exit -1

call btadmin shutdown
IF %ERRORLEVEL% NEQ 0 exit -1

for /F "delims=: tokens=2 " %%i in ('find /c "bar called" blacktie.log') do set val=%%i
IF %VAL% NEQ 2 echo "every server should been bursted with the messages" & exit -1
