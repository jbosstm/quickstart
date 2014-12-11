@echo off

echo "Running JTS recovery quickstart"

rem SUN_CONIG="-Dcom.sun.CORBA.POA.ORBServerId=1 -Dcom.sun.CORBA.POA.ORBPersistentServerPort=12567"

echo "Generating a recovery record ..."
mvn -e clean compile exec:java -Dexec.mainClass=Test -Dexec.args="-crash"
IF %ERRORLEVEL% NEQ 0 exit -1

echo "Recovering failed service - this could take up to a minute or so ..."
mvn -e exec:java -Dexec.mainClass=Test -Dexec.args="-recover -auto"
IF %ERRORLEVEL% NEQ 0 exit -1

echo "Testing that the XID data files were removed"
IF EXIST target\ExampleXAResource1.xid_ exit -1 
IF EXIST target\ExampleXAResource2.xid_ exit -1 

echo "Service recovery example succeeded"

