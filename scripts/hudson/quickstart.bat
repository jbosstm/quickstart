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

SETLOCAL EnableDelayedExpansion


call:comment_on_pull "Starting tests %BUILD_URL%"

git remote add upstream https://github.com/jbosstm/quickstart.git
set BRANCHPOINT=master
git branch %BRANCHPOINT% origin/%BRANCHPOINT%
git pull --rebase --ff-only origin %BRANCHPOINT% || (call:comment_on_pull "Rebase failed %BUILD_URL%" && exit -1)

rem INITIALIZE C++ COMPILER
call "C:\Program Files (x86)\Microsoft Visual Studio 9.0\VC\vcvarsall.bat"

rem find the ip address of the host
rem for /f "delims=" %%a in ('hostname') do @set host=%%a 
rem for /f "tokens=2 delims=[]" %%a in ('ping -n 1 %host%') do set JBOSSAS_IP_ADDR=%%a
rem INITIALIZE ENV



git clone git://github.com/jbosstm/narayana.git
cd narayana
set OLDWORKSPACE=%WORKSPACE%
set WORKSPACE=%WORKSPACE%\narayana\
set CACHED_COMMENT=%COMMENT_ON_PULL%
set COMMENT_ON_PULL=0
set NOTBT=1
set NOTMAIN=1
call build_mp
call scripts\hudson\narayana.bat -DskipTests -Pcommunity
if %ERRORLEVEL% NEQ 0 (set COMMENT_ON_PULL=%CACHED_COMMENT% & call:comment_on_pull "Narayana build failed %BUILD_URL%" & exit -1)
cd ..

set COMMENT_ON_PULL=%CACHED_COMMENT%
set WORKSPACE=%OLDWORKSPACE%
rem git checkout 4.17
rem call build.bat clean install -DskipTests

rem Downloading the WildFly nightly build
rmdir "wildfly-*" /s /q
wget --user=guest --password=guest --no-check-certificate -nv https://ci.wildfly.org/httpAuth/repository/downloadAll/WF_Nightly/.lastSuccessful/artifacts.zip
unzip -q artifacts.zip
unzip -q "wildfly-*-SNAPSHOT.zip"
rem the WildFly nightly build distro could be wrapped in two level structure
unzip -n -q "wildfly-*-SNAPSHOT.zip"
del "wildfly-*-SNAPSHOT*.zip"
dir /b wildfly-*-SNAPSHOT > filename.tmp
set /p JBOSS_DIST_DIR=<filename.tmp
set JBOSS_HOME=%CD%\%JBOSS_DIST_DIR%
echo "Downloaded WildFly nightly build and placed under JBOSS_HOME directory at '%JBOSS_HOME%'"

copy narayana\blacktie\blacktie\target\blacktie-*-vc9x32-bin.zip .
set BLACKTIE_DIST_HOME=C:\hudson\workspace\%JOB_NAME%\
wget -N http://%JENKINS_HOST%/userContent/blacktie/instantclient-basiclite-win32-11.2.0.1.0.zip
wget -N http://%JENKINS_HOST%/userContent/blacktie/instantclient-sdk-win32-11.2.0.1.0.zip

rem XTS config
copy %JBOSS_HOME%\docs\examples\configs\standalone-xts.xml %JBOSS_HOME%\standalone\configuration\

rem RTS config
copy %JBOSS_HOME%\docs\examples\configs\standalone-rts.xml %JBOSS_HOME%\standalone\configuration\

rem JBTM-2820 disable the karaf build
rem git clone https://github.com/apache/karaf apache-karaf || (call:comment_on_pull "Karaf clone failed %BUILD_URL%" & exit -1)
rem call build.bat -f apache-karaf/pom.xml -Pfastinstall || (call:comment_on_pull "Karaf build failed %BUILD_URL%" & exit -1)

echo Running quickstarts
call build.bat clean install -B || (call:comment_on_pull "Pull failed %BUILD_URL%" && exit -1)


call:comment_on_pull "Pull passed %BUILD_URL%"


rem -------------------------------------------------------
rem -                 Functions bellow                    -
rem -------------------------------------------------------

goto:eof

:build_mp
   echo "Cloning MicroProfile LRA"
   rmdir /S /Q microprofile-lra
   git clone https://github.com/jbosstm/microprofile-lra.git || (call:           comment_on_pull "MP LRA clone Failed %BUILD_URL%" & exit -1)
   if %ERRORLEVEL% NEQ 0 exit -1
   cd microprofile-lra
   git checkout microprofile-lra
   if %ERRORLEVEL% NEQ 0 exit -1
   cd ..
   call build.bat clean install "-f" "microprofile-lra\pom.xml" || (call:        comment_on_pull "MP LRA build Failed %BUILD_URL%" & exit -1)
goto:eof

:comment_on_pull
   if not "%COMMENT_ON_PULL%"=="1" goto:eof

   for /f "tokens=1,2,3,4 delims=/" %%a in ("%GIT_BRANCH%") do set IS_PULL=%%b&set PULL_NUM=%%c
   if not "%IS_PULL%"=="pull" goto:eof
   
   curl -k -d "{ \"body\": \"%~1\" }" -H "Authorization: token %GITHUB_TOKEN%" https://api.github.com/repos/%GIT_ACCOUNT%/%GIT_REPO%/issues/%PULL_NUM%/comments

goto:eof
