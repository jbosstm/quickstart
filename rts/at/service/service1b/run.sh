# ALLOW JOBS TO BE BACKGROUNDED
set -m

echo "Running service1b quickstart"
echo "Deploying service ..."
mvn clean package wildfly:deploy
[ "$?" == "0" ] || exit -1

echo "running client ..."
mvn -P client exec:java
res=$?

mvn package wildfly:undeploy

exit $res
