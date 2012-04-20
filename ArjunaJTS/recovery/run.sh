#!/bin/sh

# ALLOW JOBS TO BE BACKGROUNDED
set -m

echo "Running recovery quickstart"

# if you want to test using the sun orb you 
# SUN_CONIG="-Dcom.sun.CORBA.POA.ORBServerId=1 -Dcom.sun.CORBA.POA.ORBPersistentServerPort=12567"

echo "Generating a recovery record ..."
mvn -e clean compile exec:java -Dexec.mainClass=Test -Dexec.args="-crash"

echo "Recovering failed service - this could take up to a minute or so ..."
mvn -e exec:java -Dexec.mainClass=Test -Dexec.args="-recover -auto"

XID1="target/ExampleXAResource1.xid_"
XID2="target/ExampleXAResource2.xid_"

echo "Testing that the XID data files were removed"

[ -f $XID1 -a -f $XID2 ] && (echo "JTS example failed"; exit -1) || (echo "JTS example passed"; exit 0)
