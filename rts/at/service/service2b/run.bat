@echo off

echo "Running service2b quickstart"
echo "Deploying service ..."
mvn clean package jboss-as:deploy
IF %ERRORLEVEL% NEQ 0 exit -1

echo "running client ..."
mvn -P client exec:java
set ERR=%ERRORLEVEL%

mvn package jboss-as:undeploy

exit /B %ERR%
