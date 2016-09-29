SETLOCAL EnableDelayedExpansion

set M2_HOME=c:\hudson\tools\apache-maven-3.0.3
set ANT_HOME=C:\hudson\tools\apache-ant-1.8.2
set GNUWIN32_PATH=C:\hudson\gnuwin32
set PATH=%M2_HOME%\bin\;%ANT_HOME%\bin;%GNUWIN32_PATH%\bin;%PATH%

call:comment_on_pull "Starting tests %BUILD_URL%"

git remote add upstream https://github.com/jbosstm/quickstart.git
set BRANCHPOINT=5.2
git branch %BRANCHPOINT% origin/%BRANCHPOINT%
git pull --rebase --ff-only origin %BRANCHPOINT% || (call:comment_on_pull "Rebase failed %BUILD_URL%" && exit -1)

rem INITIALIZE C++ COMPILER
call "C:\Program Files (x86)\Microsoft Visual Studio 9.0\VC\vcvarsall.bat"

rem find the ip address of the host
rem for /f "delims=" %%a in ('hostname') do @set host=%%a 
rem for /f "tokens=2 delims=[]" %%a in ('ping -n 1 %host%') do set JBOSSAS_IP_ADDR=%%a
rem INITIALIZE ENV

set ORACLE_HOME=C:\hudson\workspace\%JOB_NAME%\instantclient_11_2
set TNS_ADMIN=C:\hudson\workspace\%JOB_NAME%\instantclient_11_2\network\admin


git clone https://github.com/jbosstm/narayana.git
cd narayana
git checkout 5.2
set OLDWORKSPACE=%WORKSPACE%
set WORKSPACE=%WORKSPACE%\narayana\
set COMMENT_ON_PULL=0
call scripts\hudson\narayana.bat -DskipTests
if %ERRORLEVEL% NEQ 0 exit -1
set COMMENT_ON_PULL=1
set WORKSPACE=%OLDWORKSPACE%

wget --no-check-certificate https://ci.jboss.org/hudson/job/WildFly-latest-master/lastBuild/artifact/dist/target/wildfly-10.x.zip
rmdir wildfly-10.*.*.Final-SNAPSHOT /s /q
unzip wildfly-10.x.zip
cd wildfly-10.*.*.Final-SNAPSHOT/
set "JBOSS_HOME=%cd%"
cd ..
copy %JBOSS_HOME%\docs\examples\configs\standalone-xts.xml %JBOSS_HOME%\standalone\configuration\
copy %JBOSS_HOME%\docs\examples\configs\standalone-rts.xml %JBOSS_HOME%\standalone\configuration\

git clone https://github.com/apache/karaf apache-karaf
cd apache-karaf
call mvn -Pfastinstall
cd ..

echo Running quickstarts
call mvn clean install || (call:comment_on_pull "Pull failed %BUILD_URL%" && exit -1)


call:comment_on_pull "Pull passed %BUILD_URL%"


rem -------------------------------------------------------
rem -                 Functions bellow                    -
rem -------------------------------------------------------

goto:eof

:comment_on_pull
   if not "%COMMENT_ON_PULL%"=="1" goto:eof

   for /f "tokens=1,2,3,4 delims=/" %%a in ("%GIT_BRANCH%") do set IS_PULL=%%b&set PULL_NUM=%%c
   if not "%IS_PULL%"=="pull" goto:eof
   
   curl -k -d "{ \"body\": \"%~1\" }" -ujbosstm-bot:%BOT_PASSWORD% https://api.github.com/repos/%GIT_ACCOUNT%/%GIT_REPO%/issues/%PULL_NUM%/comments

goto:eof