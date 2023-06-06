#!/bin/sh

# ALLOW JOBS TO BE BACKGROUNDED
set -m

OPENJDKORBPROPS="-DOrbPortabilityEnvironmentBean.orbDataClassName=com.arjuna.orbportability.internal.orbspecific.versions.javaidl_1_4 -DOrbPortabilityEnvironmentBean.orbImpleClassName=com.arjuna.orbportability.internal.orbspecific.javaidl.orb.implementations.javaidl_1_4"

[ "x$QUICKSTART_NARAYANA_VERSION" != 'x' ] &&\
  NARAYANA_VERSION_PARAM="-Dversion.narayana=${QUICKSTART_NARAYANA_VERSION}"

echo "Running jts standalone quickstart using OpenJDK ORB"
mvn -e exec:java  -Dexec.cleanupDaemonThreads=false -Dexec.mainClass=org.jboss.narayana.jta.quickstarts.TransactionExample $OPENJDKORBPROPS $NARAYANA_VERSION_PARAM

if [ "$?" != "0" ]; then
    echo jts standalone using OpenJDK ORB quickstart failed
    exit -1
fi

echo "JTS standalone example succeeded"
