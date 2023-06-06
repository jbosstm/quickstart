@echo off

echo "Running object_store quickstart"

IF NOT %QUICKSTART_NARAYANA_VERSION%x == x SET NARAYANA_VERSION_PARAM="-Dversion.narayana=${QUICKSTART_NARAYANA_VERSION}"

mvn -e compile exec:java -Dexec.mainClass=org.jboss.narayana.jta.quickstarts.VolatileStoreExample %NARAYANA_VERSION_PARAM%
IF %ERRORLEVEL% NEQ 0 exit -1

mvn -e compile exec:java -Dexec.mainClass=org.jboss.narayana.jta.quickstarts.HornetqStoreExample %NARAYANA_VERSION_PARAM%
IF %ERRORLEVEL% NEQ 0 exit -1

mvn -e compile exec:java -Dexec.mainClass=org.jboss.narayana.jta.quickstarts.FileStoreExample %NARAYANA_VERSION_PARAM%
IF %ERRORLEVEL% NEQ 0 exit -1

mvn -e compile exec:java -Dexec.mainClass=org.jboss.narayana.jta.quickstarts.JDBCStoreExample %NARAYANA_VERSION_PARAM%
IF %ERRORLEVEL% NEQ 0 exit -1
