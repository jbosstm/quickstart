@echo off

echo "Running service2 quickstart"

mvn clean compile exec:java
IF %ERRORLEVEL% NEQ 0 exit -1
