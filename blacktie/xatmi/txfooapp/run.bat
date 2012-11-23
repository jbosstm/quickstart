@echo off

rem RUN THE TXFOOAPP SERVER
IF ["%1"] EQU ["tx"] (
SHIFT

echo "Quickstart: Running txfooapp"

call generate_server -Dservice.names=TXFOOAPP -Dserver.includes="request.c ora.c DbService.c" -Dx.inc.dir="%ORACLE_HOME%\OCI\include" -Dx.lib.dir="%ORACLE_HOME%\OCI\lib\MSVC" -Dx.libs="oci" -Dx.define="ORACLE" -Dserver.name=txfooap
IF %ERRORLEVEL% NEQ 0 exit -1
call generate_client -Dclient.includes="client.c request.c ora.c cutil.c" -Dx.inc.dir="%ORACLE_HOME%\OCI\include" -Dx.lib.dir="%ORACLE_HOME%\OCI\lib\MSVC" -Dx.libs="oci" -Dx.define="ORACLE"
IF %ERRORLEVEL% NEQ 0 exit -1

set BLACKTIE_CONFIGURATION_DIR=svr
set BLACKTIE_CONFIGURATION=win32
call btadmin startup
IF %ERRORLEVEL% NEQ 0 exit -1

rem RUN THE C CLIENT
set BLACKTIE_CONFIGURATION_DIR=cli
set BLACKTIE_CONFIGURATION=win32
set LOG4CXXCONFIG=log4cxx.client.properties
client
IF %ERRORLEVEL% NEQ 0 exit -1

rem SHUTDOWN THE SERVER RUNNING THE btadmin TOOL
set BLACKTIE_CONFIGURATION_DIR=svr
set BLACKTIE_CONFIGURATION=win32
call btadmin shutdown
IF %ERRORLEVEL% NEQ 0 exit -1
set BLACKTIE_CONFIGURATION_DIR=
set BLACKTIE_CONFIGURATION=
set LOG4CXXCONFIG=
)
