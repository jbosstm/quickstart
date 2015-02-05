@echo off


echo "Running JTS standalone using JacORB"
mvn clean compile exec:java  -Dexec.cleanupDaemonThreads=false -Dexec.mainClass=org.jboss.narayana.jta.quickstarts.TransactionExample
IF %ERRORLEVEL% NEQ 0 exit -1

echo "Running JTS standalone using JdkORB"
mvn exec:java  -Dexec.cleanupDaemonThreads=false -Dexec.mainClass=org.jboss.narayana.jta.quickstarts.TransactionExample -DOrbPortabilityEnvironmentBean.orbDataClassName=com.arjuna.orbportability.internal.orbspecific.versions.javaidl_1_4 -DOrbPortabilityEnvironmentBean.orbImpleClassName=com.arjuna.orbportability.internal.orbspecific.javaidl.orb.implementations.javaidl_1_4
IF %ERRORLEVEL% NEQ 0 exit -1

echo "JTS standalone example succeeded"

