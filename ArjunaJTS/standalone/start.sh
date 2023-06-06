
# start NS
# for orbd JDK 1.8 is needed
function startNS {
  /usr/bin/orbd $PROG_ARGS
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

