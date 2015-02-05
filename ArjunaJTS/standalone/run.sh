#!/bin/sh

# ALLOW JOBS TO BE BACKGROUNDED
set -m

JDKORBPROPS="-DOrbPortabilityEnvironmentBean.orbDataClassName=com.arjuna.orbportability.internal.orbspecific.versions.javaidl_1_4 -DOrbPortabilityEnvironmentBean.orbImpleClassName=com.arjuna.orbportability.internal.orbspecific.javaidl.orb.implementations.javaidl_1_4"

echo "Running jts standalone quickstart using JacOrb"

mvn clean compile exec:java  -Dexec.cleanupDaemonThreads=false -Dexec.mainClass=org.jboss.narayana.jta.quickstarts.TransactionExample

if [ "$?" != "0" ]; then
    echo jts standalone using JacOrb quickstart failed
    exit -1
fi

echo "Running jts standalone quickstart using JdkOrb"
mvn exec:java  -Dexec.cleanupDaemonThreads=false -Dexec.mainClass=org.jboss.narayana.jta.quickstarts.TransactionExample $JDKORBPROPS

if [ "$?" != "0" ]; then
    echo jts standalone using JdkOrb quickstart failed
    exit -1
fi

echo "JTS standalone example succeeded"

