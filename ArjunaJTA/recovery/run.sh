
# ALLOW JOBS TO BE BACKGROUNDED
set -m

echo "Running recovery quickstart"

[ "x$QUICKSTART_NARAYANA_VERSION" != 'x' ] &&\
  NARAYANA_VERSION_PARAM="-Dversion.narayana=${QUICKSTART_NARAYANA_VERSION}"

# To run an example use the maven java exec pluging. For example to run the second recovery example
mvn -e exec:java -Dexec.mainClass=org.jboss.narayana.jta.quickstarts.recovery.DummyRecovery -Dexec.args="-f" $NARAYANA_VERSION_PARAM
# We expect this to fail
#if [ "$?" != "0" ]; then
#	exit -1
#fi
mvn -e exec:java -Dexec.mainClass=org.jboss.narayana.jta.quickstarts.recovery.DummyRecovery -Dexec.args="-r" $NARAYANA_VERSION_PARAM
if [ "$?" != "0" ]; then
    echo "Dummy example failed"
	exit -1
else
    echo "Dummy example succeeded"
fi

# And to run the JMS recovery example:
mvn -e exec:java -Dexec.mainClass=org.jboss.narayana.jta.quickstarts.recovery.JmsRecovery -Dexec.args="-f" $NARAYANA_VERSION_PARAM
# We expect this to fail
#if [ "$?" != "0" ]; then
#	exit -1
#fi
mvn -e exec:java -Dexec.mainClass=org.jboss.narayana.jta.quickstarts.recovery.JmsRecovery -Dexec.args="-r" $NARAYANA_VERSION_PARAM
if [ "$?" != "0" ]; then
	exit -1
    echo "JMS example failed"
else
    echo "JMS example succeeded"
fi

echo "All recovery examples succeeded"
