#/bin/bash
set -m

export QUICKSTART_NAME=${PWD##*/}
TOMCAT_VERSION=7.0.82
wget -nc https://archive.apache.org/dist/tomcat/tomcat-7/v$TOMCAT_VERSION/bin/apache-tomcat-$TOMCAT_VERSION.zip
rm -rf apache-tomcat-$TOMCAT_VERSION
unzip apache-tomcat-$TOMCAT_VERSION.zip
export TOMCAT_HOME=$(pwd)/apache-tomcat-$TOMCAT_VERSION/
chmod +x $TOMCAT_HOME/bin/catalina.sh
mvn package
rm -rf $TOMCAT_HOME/webapps/${QUICKSTART_NAME}/
cp target/${QUICKSTART_NAME}.war $TOMCAT_HOME/webapps/
JPDA_SUSPEND=n $TOMCAT_HOME/bin/catalina.sh jpda run &
sleep 10

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
sleep 5
x=`curl -s http://localhost:8080/${QUICKSTART_NAME}/recovery`

$TOMCAT_HOME/bin/catalina.sh stop
rm -rf apache-tomcat-$TOMCAT_VERSION

if [ "$x" != "[\"crash\"]" ]; then
    echo "Crash and Recovery failed"
    exit -1
fi

echo "All tests succeeded"
