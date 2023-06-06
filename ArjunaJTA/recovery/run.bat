@echo off

echo "Running recovery quickstart"

IF NOT %QUICKSTART_NARAYANA_VERSION%x == x SET NARAYANA_VERSION_PARAM="-Dversion.narayana=${QUICKSTART_NARAYANA_VERSION}"

rem To run an example use the maven java exec pluging. For example to run the second recovery example
rem (need to run with cmd /c as mvn exec:java does not fork the process and the failure ends the current bat process)
cmd /c mvn -e compile exec:java %NARAYANA_VERSION_PARAM% -Dexec.mainClass=org.jboss.narayana.jta.quickstarts.recovery.DummyRecovery -Dexec.args="-f"
rem We expect to fail as halt of JVM happens
rem IF %ERRORLEVEL% NEQ 0 exit -1

mvn -e compile exec:java %NARAYANA_VERSION_PARAM% -Dexec.mainClass=org.jboss.narayana.jta.quickstarts.recovery.DummyRecovery -Dexec.args="-r"
IF %ERRORLEVEL% NEQ 0 exit -1
echo "Dummy recovery example succeeded"


rem And to run the JMS recovery example:
cmd /c mvn -e compile exec:java %NARAYANA_VERSION_PARAM% -Dexec.mainClass=org.jboss.narayana.jta.quickstarts.recovery.JmsRecovery -Dexec.args="-f"
rem IF %ERRORLEVEL% NEQ 0 exit -1

mvn -e compile exec:java %NARAYANA_VERSION_PARAM% -Dexec.mainClass=org.jboss.narayana.jta.quickstarts.recovery.JmsRecovery -Dexec.args="-r"
IF %ERRORLEVEL% NEQ 0 exit -1
echo "JMS recovery example succeeded"

echo "All recovery examples succeeded"

