@echo off

echo "Quickstart: Running integration 1 XATMI"

rem RUN THE INTEGRATION 1 EXAMPLE
cd %BLACKTIE_HOME%\quickstarts\integration1\xatmi_service\
call generate_server -Dservice.names=CREDITEXAMPLE,DEBITEXAMPLE -Dserver.includes="CreditService.c,DebitService.c" -Dserver.name=integra
IF %ERRORLEVEL% NEQ 0 exit -1
call btadmin startup
IF %ERRORLEVEL% NEQ 0 exit -1
cd %BLACKTIE_HOME%\quickstarts\integration1\client\
call generate_client -Dclient.includes=client.c 
.\client 
IF %ERRORLEVEL% NEQ 0 exit -1
cd %BLACKTIE_HOME%\quickstarts\integration1\xatmi_service\
call btadmin shutdown
IF %ERRORLEVEL% NEQ 0 exit -1

cd %BLACKTIE_HOME%\quickstarts\integration1\ejb
call mvn install
IF %ERRORLEVEL% NEQ 0 exit -1
cd %BLACKTIE_HOME%\quickstarts\integration1\ejb\ear
call mvn install jboss-as:deploy
IF %ERRORLEVEL% NEQ 0 exit -1
cd %BLACKTIE_HOME%\quickstarts\integration1\xatmi_adapter\
call mvn install
IF %ERRORLEVEL% NEQ 0 exit -1
cd %BLACKTIE_HOME%\quickstarts\integration1\xatmi_adapter\ear\
call mvn install jboss-as:deploy
IF %ERRORLEVEL% NEQ 0 exit -1

cd %BLACKTIE_HOME%\quickstarts\integration1\client\
call generate_client -Dclient.includes=client.c 
IF %ERRORLEVEL% NEQ 0 exit -1
cd %BLACKTIE_HOME%\quickstarts\integration1\client\
@ping 127.0.0.1 -n 10 -w 1000 > nul
.\client
IF %ERRORLEVEL% NEQ 0 exit -1
cd %BLACKTIE_HOME%\quickstarts\integration1\xatmi_adapter\ear\
call mvn jboss-as:undeploy
IF %ERRORLEVEL% NEQ 0 exit -1
cd %BLACKTIE_HOME%\quickstarts\integration1\ejb\ear
call mvn jboss-as:undeploy
IF %ERRORLEVEL% NEQ 0 exit -1
