# ALLOW JOBS TO BE BACKGROUNDED
set -m

echo "Running service2b quickstart"
echo "Deploying service ..."
mvn clean package jboss-as:deploy
[ "$?" == "0" ] || exit -1

echo "running client ..."
mvn -P client exec:java
res=$?

mvn package jboss-as:undeploy

exit $res
