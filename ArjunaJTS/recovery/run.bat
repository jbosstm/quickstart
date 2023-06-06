@echo off

echo "Running JTS recovery quickstart"

OPENJDK_CONFIG="-Dcom.sun.CORBA.POA.ORBServerId=1 -Dcom.sun.CORBA.POA.ORBPersistentServerPort=12567"

IF NOT %QUICKSTART_NARAYANA_VERSION%x == x SET NARAYANA_VERSION_PARAM="-Dversion.narayana=${QUICKSTART_NARAYANA_VERSION}"

echo "Generating a recovery record ..."
mvn -e compile exec:java -Dexec.mainClass=Test -Dexec.args="-crash" %OPENJDK_CONFIG %NARAYANA_VERSION_PARAM%
IF %ERRORLEVEL% NEQ 0 exit -1

echo "Recovering failed service - this could take up to a minute or so ..."
mvn -e exec:java -Dexec.mainClass=Test -Dexec.args="-recover -auto" %OPENJDK_CONFIG %NARAYANA_VERSION_PARAM%
IF %ERRORLEVEL% NEQ 0 exit -1

echo "Testing that the XID data files were removed"
IF EXIST target\ExampleXAResource1.xid_ exit -1 
IF EXIST target\ExampleXAResource2.xid_ exit -1 

echo "Service recovery example succeeded"

