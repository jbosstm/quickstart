@echo off

echo "Running service1 quickstart"

mvn clean compile exec:java
IF %ERRORLEVEL% NEQ 0 exit -1
