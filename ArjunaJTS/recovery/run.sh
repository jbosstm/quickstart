#!/bin/sh

# ALLOW JOBS TO BE BACKGROUNDED
set -m

echo "Running recovery quickstart"

# if you want to test using the sun orb you 
OPENJDK_CONFIG="-Dcom.sun.CORBA.POA.ORBServerId=1 -Dcom.sun.CORBA.POA.ORBPersistentServerPort=12567"

[ "x$QUICKSTART_NARAYANA_VERSION" != 'x' ] &&\
  NARAYANA_VERSION_PARAM="-Dversion.narayana=${QUICKSTART_NARAYANA_VERSION}"

echo "Generating a recovery record ..."
mvn -e exec:java -Dexec.mainClass=Test -Dexec.args="-crash" $OPENJDK_CONFIG $NARAYANA_VERSION_PARAM

echo "Recovering failed service - this could take up to a minute or so ..."
mvn -e exec:java -Dexec.mainClass=Test -Dexec.args="-recover -auto" $OPENJDK_CONFIG $NARAYANA_VERSION_PARAM

XID1="target/ExampleXAResource1.xid_"
XID2="target/ExampleXAResource2.xid_"

echo "Testing that the XID data files were removed"

if [ -f $XID1 -a -f $XID2 ]; then
  echo "JTS example failed"
  exit -1
else
  echo "JTS example passed"
fi
