
# JBoss, Home of Professional Open Source
# Copyright 2016, Red Hat, Inc., and others contributors as indicated
# by the @authors tag. All rights reserved.
# See the copyright.txt in the distribution for a
# full listing of individual contributors.
# This copyrighted material is made available to anyone wishing to use,
# modify, copy, or redistribute it subject to the terms and conditions
# of the GNU Lesser General Public License, v. 2.1.
# This program is distributed in the hope that it will be useful, but WITHOUT A
# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
# PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
# You should have received a copy of the GNU Lesser General Public License,
# v.2.1 along with this distribution; if not, write to the Free Software
# Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
# MA  02110-1301, USA.

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

