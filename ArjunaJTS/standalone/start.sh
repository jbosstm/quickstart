
# start NS
function startNS {
  if [ $ORBTYPE = "jacorb" ]; then
#NSFCONFIG="-Djacorb.naming.ior_filename=$NS_FILE"
    java -cp "${JACORB_LIB_DIR}/*" org.jacorb.naming.NameServer -Dorg.omg.CORBA.ORBClass=org.jacorb.orb.ORB -Djacorb.net.server_socket_factory=org.jacorb.orb.factory.PortRangeServerSocketFactory -Djacorb.net.server_socket_factory.port.min=9999 -Djacorb.net.server_socket_factory.port.max=9999 -Djacorb.log.default.verbosity=4 -DOAIAddr=$HOST_ADDRESS
  elif [ $ORBTYPE = "jdkorb" ]; then
    /usr/bin/orbd $PROG_ARGS
  fi
}

# start TM
function startTM {
  export CLASSPATH="$NARAYANA_HOME/lib/jts\*"
  DBGOPTS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"

  if [ $ORBTYPE = "jacorb" ]; then
    . $NARAYANA_HOME/jts-${ORBTYPE}-setup-env.sh; java $DBGOPTS -DOrbPortabilityEnvironmentBean.bindMechanism="NAME_SERVICE" -DOrbPortabilityEnvironmentBean.resolveService="NAME_SERVICE" com.arjuna.ats.jts.TransactionServer -ORBInitRef.NameService="corbaloc::$HOST_ADDRESS:9999/StandardNS/NameServer-POA/_root"
  elif [ $ORBTYPE = "jdkorb" ]; then
    java $DBGOPTS -cp "$NARAYANA_HOME/lib/jts/*:$NARAYANA_HOME/lib/ext/*" $JDKORBPROPS $NSPROPS com.arjuna.ats.jts.TransactionServer $PROG_ARGS
  fi
}

# start RM
function startRM {
  if [ $ORBTYPE = "jacorb" ]; then
    . $NARAYANA_HOME/jts-${ORBTYPE}-setup-env.sh; java com.arjuna.ats.arjuna.recovery.RecoveryManager -ORBInitRef.NameService="corbaloc::$HOST_ADDRESS:9999/StandardNS/NameServer-POA/_root"
  else
    java -cp "$NARAYANA_HOME/lib/jts/narayana-jts-idlj.jar:$NARAYANA_HOME/lib/ext/*" $JDKORBPROPS $NSPROPS com.arjuna.ats.arjuna.recovery.RecoveryManager $PROG_ARGS
  fi
}

# start client
function startClient {
  if [ $ORBTYPE = "jacorb" ]; then
    mvn compile exec:java  -Dexec.cleanupDaemonThreads=false -Dexec.mainClass=org.jboss.narayana.jta.quickstarts.RemoteTMExample -DOrbPortabilityEnvironmentBean.orbImpleClassName=com.arjuna.orbportability.internal.orbspecific.jacorb.orb.implementations.jacorb_2_0 -DNAME_SERVER_HOST="$HOST_ADDRESS"
  else
    mvn compile exec:java  -Dexec.cleanupDaemonThreads=false -Dexec.mainClass=org.jboss.narayana.jta.quickstarts.RemoteTMExample -DOrbPortabilityEnvironmentBean.orbImpleClassName="com.arjuna.orbportability.internal.orbspecific.javaidl.orb.implementations.javaidl_1_4" -DNAME_SERVER_HOST="$HOST_ADDRESS"
  fi
}

HOST_ADDRESS=`ip addr | grep 'state UP' -A2 | tail -n1 | awk '{print $2}' | cut -f1  -d'/'`

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
if [ $# -lt 2 ]; then
  echo "syntax {jdkorb|jacorb} {NS | TM | RM | CL}"
  exit 1
fi

JACORB_LIB_DIR=$NARAYANA_HOME/jacorb/lib
PROG_ARGS="-ORBInitialHost $HOST_ADDRESS -ORBInitialPort 9999"
NSPROPS="-DOrbPortabilityEnvironmentBean.bindMechanism=NAME_SERVICE -DOrbPortabilityEnvironmentBean.resolveService=NAME_SERVICE"
JDKORBPROPS="-DOrbPortabilityEnvironmentBean.orbDataClassName=com.arjuna.orbportability.internal.orbspecific.versions.javaidl_1_4 -DOrbPortabilityEnvironmentBean.orbImpleClassName=com.arjuna.orbportability.internal.orbspecific.javaidl.orb.implementations.javaidl_1_4"

#ORBTYPE=jdkorb
#ORBTYPE=jacorb
ORBTYPE=$1

case $2 in
"NS") echo "starting NS"; startNS ;;
"TM") echo "starting TM"; startTM ;;
"RM") echo "starting RM"; startRM ;;
"CL") echo "starting CL"; startClient ;;
esac

