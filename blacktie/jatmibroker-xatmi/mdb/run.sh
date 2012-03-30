# ALLOW JOBS TO BE BACKGROUNDED
set -m

echo "Running MDB quickstart"

# RUN THE MDB EXAMPLE
(cd $BLACKTIE_HOME/quickstarts/mdb && mvn clean install -DskipTests)
if [ "$?" != "0" ]; then
	exit -1
fi
(cd $BLACKTIE_HOME/quickstarts/mdb/ear && mvn clean install jboss-as:deploy)
if [ "$?" != "0" ]; then
	exit -1
fi
sleep 10
(cd $BLACKTIE_HOME/quickstarts/mdb && mvn surefire:test)
if [ "$?" != "0" ]; then
	exit -1
fi
(cd $BLACKTIE_HOME/quickstarts/mdb/ear && mvn jboss-as:undeploy)
if [ "$?" != "0" ]; then
	exit -1
fi

