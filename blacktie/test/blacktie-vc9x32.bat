set NOPAUSE=true

rem SHUTDOWN ANY PREVIOUS BUILD REMNANTS
if exist %WORKSPACE%\jboss-as-7.2.0.Alpha1-SNAPSHOT call %WORKSPACE%\jboss-as-7.2.0.Alpha1-SNAPSHOT\bin\jboss-cli.bat --connect command=:shutdown && cd .
if exist %WORKSPACE%\jboss-as-7.2.0.Alpha1-SNAPSHOT @ping 127.0.0.1 -n 10 -w 1000 > nul
tasklist
taskkill /F /IM mspdbsrv.exe
taskkill /F /IM testsuite.exe
taskkill /F /IM server.exe
taskkill /F /IM client.exe
taskkill /F /IM cs.exe
tasklist

rem INITIALIZE JBOSS
call ant -f %WORKSPACE%/blacktie/test/initializeJBoss.xml -DJBOSSAS_BIN_DIR=%JBOSSAS_BIN_DIR% -Dbasedir=%WORKSPACE% initializeDatabase initializeJBoss -debug
IF %ERRORLEVEL% NEQ 0 exit -1

cd %WORKSPACE%\jboss-as-7.2.0.Alpha1-SNAPSHOT\bin\
call add-user admin password --silent=true
IF %ERRORLEVEL% NEQ 0 exit -1
call add-user guest password -a --silent=true
IF %ERRORLEVEL% NEQ 0 exit -1
call add-user dynsub password -a --silent=true
IF %ERRORLEVEL% NEQ 0 exit -1


rem START JBOSS
cd %WORKSPACE%\jboss-as-7.2.0.Alpha1-SNAPSHOT\bin
start /B standalone.bat -c standalone-full.xml -Djboss.bind.address=%JBOSSAS_IP_ADDR% -Djboss.bind.address.unsecure=%JBOSSAS_IP_ADDR%
echo "Started server"
@ping 127.0.0.1 -n 20 -w 1000 > nul

rem CREATE BLACKTIE DISTRIBUTION
for /f "delims=" %%a in ('hostname') do @set MACHINE_ADDR=%%a
call ant -f %WORKSPACE%/blacktie/test/initializeBlackTie.xml -DBT_HOME=%WORKSPACE%\blacktie\target\dist\ -DVERSION=5.0.0.M2-SNAPSHOT -DMACHINE_ADDR=%MACHINE_ADDR% -DJBOSSAS_IP_ADDR=%JBOSSAS_IP_ADDR% -DJBOSS_HOME=%WORKSPACE%\jboss-as-7.2.0.Alpha1-SNAPSHOT -DBLACKTIE_DIST_HOME=%BLACKTIE_DIST_HOME%
IF %ERRORLEVEL% NEQ 0 echo "Failing build 3" & tasklist & call %WORKSPACE%\jboss-as-7.2.0.Alpha1-SNAPSHOT\bin\jboss-cli.bat --connect command=:shutdown & @ping 127.0.0.1 -n 10 -w 1000 > nul & exit -1

rem RUN THE SAMPLES

set PATH=%PATH%;%ORACLE_HOME%\bin;%ORACLE_HOME%\vc9

echo calling generated setenv - error %ERRORLEVEL%
call %WORKSPACE%\blacktie\target\dist\blacktie-5.0.0.M2-SNAPSHOT\setenv.bat
IF %ERRORLEVEL% NEQ 0 echo "Failing build 5 with error %ERRORLEVEL%" & tasklist & call %WORKSPACE%\jboss-as-7.2.0.Alpha1-SNAPSHOT\bin\jboss-cli.bat --connect command=:shutdown & @ping 127.0.0.1 -n 10 -w 1000 > nul & exit -1

cd %WORKSPACE%\blacktie\
call run_all_quickstarts.bat tx
IF %ERRORLEVEL% NEQ 0 echo "Failing build 6 with error %ERRORLEVEL%" & tasklist & call %WORKSPACE%\jboss-as-7.2.0.Alpha1-SNAPSHOT\bin\jboss-cli.bat --connect command=:shutdown & @ping 127.0.0.1 -n 10 -w 1000 > nul & exit -1

rem SHUTDOWN ANY PREVIOUS BUILD REMNANTS
tasklist & call %WORKSPACE%\jboss-as-7.2.0.Alpha1-SNAPSHOT\bin\jboss-cli.bat --connect command=:shutdown & @ping 127.0.0.1 -n 10 -w 1000 > nul
echo "Finished build"
