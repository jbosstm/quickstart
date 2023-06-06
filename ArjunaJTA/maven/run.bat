@echo off

echo "Running maven quickstart"

IF NOT %QUICKSTART_NARAYANA_VERSION%x == x SET NARAYANA_VERSION_PARAM="-Dversion.narayana=${QUICKSTART_NARAYANA_VERSION}"

mvn compile exec:exec %NARAYANA_VERSION_PARAM%
IF %ERRORLEVEL% NEQ 0 exit -1
