@echo off

echo "Running simple quickstart"

mvn compile exec:java
IF %ERRORLEVEL% NEQ 0 exit -1
