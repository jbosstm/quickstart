@echo off

echo "Running MDB quickstart"

rem RUN THE MDB EXAMPLE
cd %BLACKTIE_HOME%\quickstarts\mdb
call mvn clean install -DskipTests
IF %ERRORLEVEL% NEQ 0 exit -1
cd %BLACKTIE_HOME%\quickstarts\mdb\ear
call mvn clean install jboss-as:deploy
IF %ERRORLEVEL% NEQ 0 exit -1
@ping 127.0.0.1 -n 5 -w 1000 > nul
call mvn surefire:test
IF %ERRORLEVEL% NEQ 0 exit -1
cd %BLACKTIE_HOME%\quickstarts\mdb\ear
call mvn jboss-as:undeploy
IF %ERRORLEVEL% NEQ 0 exit -1

