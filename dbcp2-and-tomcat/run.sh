#/bin/bash
set -m

export QUICKSTART_NAME=${PWD##*/}
TOMCAT_VERSION=9.0.7
wget -nc https://archive.apache.org/dist/tomcat/tomcat-9/v$TOMCAT_VERSION/bin/apache-tomcat-$TOMCAT_VERSION.zip
rm -rf apache-tomcat-$TOMCAT_VERSION
unzip apache-tomcat-$TOMCAT_VERSION.zip
cp ~/.m2/repository/org/jboss/spec/javax/transaction/jboss-transaction-api_1.2_spec/1.0.0.Final/jboss-transaction-api_1.2_spec-1.0.0.Final.jar apache-tomcat-$TOMCAT_VERSION/lib/
export TOMCAT_HOME=$(pwd)/apache-tomcat-$TOMCAT_VERSION/
chmod +x $TOMCAT_HOME/bin/catalina.sh

mvn package
rm -rf $TOMCAT_HOME/webapps/${QUICKSTART_NAME}/
cp target/${QUICKSTART_NAME}.war $TOMCAT_HOME/webapps/
export JPDA_SUSPEND=n
$TOMCAT_HOME/bin/catalina.sh jpda run &
if [ "$JPDA_SUSPEND" != "n" ]; then
  read -p "Press enter to continue" CONTINUE
else
  sleep 10
fi

for i in {1..10}
do
  curl -f --data "test$i" http://localhost:8080/${QUICKSTART_NAME}
done

curl -f http://localhost:8080/${QUICKSTART_NAME}

# remove all strings
curl -f -X DELETE http://localhost:8080/${QUICKSTART_NAME}

# crash the application
curl -f --data "crash" http://localhost:8080/${QUICKSTART_NAME}/crash

# restart the Tomcat
JPDA_SUSPEND=n $TOMCAT_HOME/bin/catalina.sh jpda run &

# verify the recovery
if [ "$JPDA_SUSPEND" != "n" ]; then
  read -p "Press enter to continue" CONTINUE
else
  sleep 5
fi
x=`curl -s http://localhost:8080/${QUICKSTART_NAME}/recovery`

$TOMCAT_HOME/bin/catalina.sh stop
rm -rf apache-tomcat-$TOMCAT_VERSION

if [ "$x" != "[\"crash\"]" ]; then
    echo "Crash and Recovery failed"
    exit -1
fi

echo "All tests succeeded"
