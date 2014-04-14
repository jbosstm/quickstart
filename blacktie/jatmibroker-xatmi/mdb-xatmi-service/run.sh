# ALLOW JOBS TO BE BACKGROUNDED
set -m

echo "Running MDB quickstart"

# RUN THE MDB EXAMPLE
mvn clean install -DskipTests
if [ "$?" != "0" ]; then
	exit -1
fi
(cd ear && mvn clean install wildfly:deploy)
if [ "$?" != "0" ]; then
	exit -1
fi
sleep 10
mvn surefire:test
if [ "$?" != "0" ]; then
	exit -1
fi
(cd ear && mvn wildfly:undeploy)
if [ "$?" != "0" ]; then
	exit -1
fi

