#!/bin/sh

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

# ALLOW JOBS TO BE BACKGROUNDED
set -m

echo "Running recovery quickstart"

# if you want to test using the sun orb you 
# SUN_CONIG="-Dcom.sun.CORBA.POA.ORBServerId=1 -Dcom.sun.CORBA.POA.ORBPersistentServerPort=12567"

[ "x$QUICKSTART_NARAYANA_VERSION" != 'x' ] &&\
  NARAYANA_VERSION_PARAM="-Dversion.narayana=${QUICKSTART_NARAYANA_VERSION}"

echo "Generating a recovery record ..."
mvn -e exec:java -Dexec.mainClass=Test -Dexec.args="-crash" $NARAYANA_VERSION_PARAM

echo "Recovering failed service - this could take up to a minute or so ..."
mvn -e exec:java -Dexec.mainClass=Test -Dexec.args="-recover -auto" $NARAYANA_VERSION_PARAM

XID1="target/ExampleXAResource1.xid_"
XID2="target/ExampleXAResource2.xid_"

echo "Testing that the XID data files were removed"

if [ -f $XID1 -a -f $XID2 ]; then
  echo "JTS example failed"
  exit -1
else
  echo "JTS example passed"
fi
