rem JBoss, Home of Professional Open Source
rem Copyright 2016, Red Hat, Inc., and others contributors as indicated
rem by the @authors tag. All rights reserved.
rem See the copyright.txt in the distribution for a
rem full listing of individual contributors.
rem This copyrighted material is made available to anyone wishing to use,
rem modify, copy, or redistribute it subject to the terms and conditions
rem of the GNU Lesser General Public License, v. 2.1.
rem This program is distributed in the hope that it will be useful, but WITHOUT A
rem WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
rem PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
rem You should have received a copy of the GNU Lesser General Public License,
rem v.2.1 along with this distribution; if not, write to the Free Software
rem Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
rem MA  02110-1301, USA.

set NOPAUSE=true

if not defined WORKSPACE echo "WORKSPACE not set" & exit -1

if not defined JBOSSAS_IP_ADDR echo "JBOSSAS_IP_ADDR not set" & for /f "delims=" %%a in ('hostname') do @set JBOSSAS_IP_ADDR=%%a

rem SHUTDOWN ANY PREVIOUS BUILD REMNANTS
if exist jboss-as\bin\jboss-cli.bat call jboss-as\bin\jboss-cli.bat --connect command=:shutdown && cd .
if exist jboss-as\bin\jboss-cli.bat @ping 127.0.0.1 -n 10 -w 1000 > nul
tasklist
taskkill /F /IM mspdbsrv.exe
taskkill /F /IM testsuite.exe
taskkill /F /IM server.exe
taskkill /F /IM client.exe
taskkill /F /IM cs.exe
tasklist

rem INITIALIZE JBOSS and CREATE BLACKTIE DISTRIBUTION
for /f "delims=" %%a in ('hostname') do @set MACHINE_ADDR=%%a
call ant -f %WORKSPACE%/blacktie/test/initializeBlackTie.xml -DJBOSS_HOME=%JBOSS_HOME% -DBT_HOME=%WORKSPACE%\blacktie\target\dist\ -DVERSION=5.11.0.Final -DMACHINE_ADDR=%MACHINE_ADDR% -DJBOSSAS_IP_ADDR=%JBOSSAS_IP_ADDR% -DBLACKTIE_DIST_HOME=%BLACKTIE_DIST_HOME%  -Dbasedir=%WORKSPACE% initializeDatabase initializeJBoss -debug
IF %ERRORLEVEL% NEQ 0 echo "Failing build 3" & tasklist & call jboss-as\bin\jboss-cli.bat --connect command=:shutdown & @ping 127.0.0.1 -n 10 -w 1000 > nul & exit -1
# INITIALIZE JBOSS
call ant -f %WORKSPACE%/narayana/blacktie/scripts/hudson/initializeJBoss.xml -DJBOSS_HOME=%WORKSPACE%\jboss-as initializeJBoss
set JBOSS_HOME=

rem START JBOSS
cd %WORKSPACE%\jboss-as\bin
start /B standalone.bat -c standalone-blacktie.xml -Djboss.bind.address=%JBOSSAS_IP_ADDR% -Djboss.bind.address.unsecure=%JBOSSAS_IP_ADDR%
echo "Started server"
@ping 127.0.0.1 -n 20 -w 1000 > nul

rem TWEAK txfooapp FOR THIS NODE
call ant -f %WORKSPACE%/blacktie/test/initializeBlackTie.xml tweak-txfooapp-for-environment
IF %ERRORLEVEL% NEQ 0 echo "Failing build 3" & tasklist & call jboss-as\bin\jboss-cli.bat --connect command=:shutdown & @ping 127.0.0.1 -n 10 -w 1000 > nul & exit -1

rem RUN THE SAMPLES

set PATH=%PATH%;%ORACLE_HOME%\bin;%ORACLE_HOME%\vc9

echo calling generated setenv - error %ERRORLEVEL%
call %WORKSPACE%\blacktie\target\dist\blacktie-5.11.0.Final\setenv.bat
IF %ERRORLEVEL% NEQ 0 echo "Failing build 5 with error %ERRORLEVEL%" & tasklist & call jboss-as\bin\jboss-cli.bat --connect command=:shutdown & @ping 127.0.0.1 -n 10 -w 1000 > nul & exit -1

cd %WORKSPACE%\blacktie\
call run_all_quickstarts.bat
IF %ERRORLEVEL% NEQ 0 echo "Failing build 6 with error %ERRORLEVEL%" & tasklist & call jboss-as\bin\jboss-cli.bat --connect command=:shutdown & @ping 127.0.0.1 -n 10 -w 1000 > nul & exit -1

rem SHUTDOWN ANY PREVIOUS BUILD REMNANTS
tasklist & call jboss-as\bin\jboss-cli.bat --connect command=:shutdown & @ping 127.0.0.1 -n 10 -w 1000 > nul
FOR /F "usebackq tokens=5" %%i in (`"netstat -ano|findstr 9990.*LISTENING"`) DO taskkill /F /PID %%i
echo "Finished build"
