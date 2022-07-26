
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


# start NS
function startNS {
  export MAVEN_OPTS="--Dexec.cleanupDaemonThreads=false -Dcom.sun.CORBA.POA.ORBPersistentServerPort=12567  \
  -Dorg.omg.CORBA.ORBInitialHost=$HOST_ADDRESS  -Dorg.omg.CORBA.ORBInitialPort=900"
  mvn clean compile exec:java -Pnameserver -Dexec.mainClass=com.sun.corba.se.impl.naming.pcosnaming.NameServer 
  unset MAVEN_OPTS

}

# start TM
function startTM {
  export CLASSPATH="$NARAYANA_HOME/lib/jts\*"
  DBGOPTS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"

  java $DBGOPTS -cp "$NARAYANA_HOME/lib/jts/*:$NARAYANA_HOME/lib/ext/*" $OPENJDKORBPROPS $NSPROPS com.arjuna.ats.jts.TransactionServer $PROG_ARGS

}

# start RM
function startRM {

  java -cp "$NARAYANA_HOME/lib/jts/narayana-jts-idlj.jar:$NARAYANA_HOME/lib/ext/*" $OPENJDKORBPROPS $NSPROPS com.arjuna.ats.arjuna.recovery.RecoveryManager $PROG_ARGS

}

# start client
function startClient {
    mvn compile exec:java  -Dexec.cleanupDaemonThreads=false -Dexec.mainClass=org.jboss.narayana.jta.quickstarts.RemoteTMExample -DOrbPortabilityEnvironmentBean.orbImpleClassName="com.arjuna.orbportability.internal.orbspecific.javaidl.orb.implementations.javaidl_1_4" -DNAME_SERVER_HOST="$HOST_ADDRESS"
}

HOST_ADDRESS=${HOST_ADDRESS:-`ip addr | grep 'state UP' -A2 | tail -n1 | awk '{print $2}' | cut -f1  -d'/'`}

if test "x$JAVA_HOME" = "x"; then
  echo Please ensure the JAVA_HOME environment variable is set
  exit 1
fi
if test "x$NARAYANA_HOME" = "x"; then
  echo Please ensure the NARAYANA_HOME environment variable is set to the location of the narayana distribution
  exit 1
fi
if test "x$HOST_ADDRESS" = "x"; then
  echo Please ensure the HOST_ADDRESS environment variable is set to the IP address of one of your network interfaces
  exit 1
fi
if [ $# -lt 1 ]; then
  echo "syntax {NS | TM | RM | CL}"
  exit 1
fi

PROG_ARGS="-ORBInitialHost $HOST_ADDRESS -ORBInitialPort 9999"
NSPROPS="-DOrbPortabilityEnvironmentBean.bindMechanism=NAME_SERVICE -DOrbPortabilityEnvironmentBean.resolveService=NAME_SERVICE"
OPENJDKORBPROPS="-DOrbPortabilityEnvironmentBean.orbDataClassName=com.arjuna.orbportability.internal.orbspecific.versions.javaidl_1_4 -DOrbPortabilityEnvironmentBean.orbImpleClassName=com.arjuna.orbportability.internal.orbspecific.javaidl.orb.implementations.javaidl_1_4"

case $1 in
"NS") echo "starting NS"; startNS ;;
"TM") echo "starting TM"; startTM ;;
"RM") echo "starting RM"; startRM ;;
"CL") echo "starting CL"; startClient ;;
esac

