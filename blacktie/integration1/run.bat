@echo off

echo "Quickstart: Running integration 1 XATMI"

rem RUN THE INTEGRATION 1 EXAMPLE
cd xatmi_service\
call generate_server -Dservice.names=CREDITEXAMPLE,DEBITEXAMPLE -Dserver.includes="CreditService.c,DebitService.c" -Dserver.name=integra
IF %ERRORLEVEL% NEQ 0 exit -1
call btadmin startup
IF %ERRORLEVEL% NEQ 0 exit -1
cd ..\client\
call generate_client -Dclient.includes=client.c 
.\client 
IF %ERRORLEVEL% NEQ 0 exit -1
cd ..\xatmi_service\
call btadmin shutdown
IF %ERRORLEVEL% NEQ 0 exit -1

cd ..\ejb
call mvn install
IF %ERRORLEVEL% NEQ 0 exit -1
cd ear
call mvn install wildfly:deploy
IF %ERRORLEVEL% NEQ 0 exit -1
cd ..\..\xatmi_adapter\
call mvn install
IF %ERRORLEVEL% NEQ 0 exit -1
cd ear\
call mvn install wildfly:deploy
IF %ERRORLEVEL% NEQ 0 exit -1

cd ..\..\client\
call generate_client -Dclient.includes=client.c 
IF %ERRORLEVEL% NEQ 0 exit -1
@ping 127.0.0.1 -n 10 -w 1000 > nul
.\client
IF %ERRORLEVEL% NEQ 0 exit -1
cd ..\xatmi_adapter\ear\
call mvn wildfly:undeploy
IF %ERRORLEVEL% NEQ 0 exit -1
cd ..\..\ejb\ear
call mvn wildfly:undeploy
IF %ERRORLEVEL% NEQ 0 exit -1
