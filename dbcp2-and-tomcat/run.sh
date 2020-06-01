#/bin/bash
set -m
set -x

CURL_IP_OPTS=""
IP_OPTS="${IPV6_OPTS}" # use setup of IPv6 if it's defined, otherwise go with IPv4
if [ -z "$IP_OPTS" ]; then
  IP_OPTS="-Djava.net.preferIPv4Stack=true -Djava.net.preferIPv4Addresses"
  CURL_IP_OPTS="-4"
fi

export QUICKSTART_NAME=${PWD##*/}
TOMCAT_VERSION=9.0.34
wget -nc -nv https://archive.apache.org/dist/tomcat/tomcat-9/v$TOMCAT_VERSION/bin/apache-tomcat-$TOMCAT_VERSION.zip
rm -rf apache-tomcat-$TOMCAT_VERSION
unzip apache-tomcat-$TOMCAT_VERSION.zip
cp ~/.m2/repository/org/jboss/spec/javax/transaction/jboss-transaction-api_1.2_spec/1.0.0.Final/jboss-transaction-api_1.2_spec-1.0.0.Final.jar apache-tomcat-$TOMCAT_VERSION/lib/
export TOMCAT_HOME="$(pwd)/apache-tomcat-$TOMCAT_VERSION"
chmod +x "${TOMCAT_HOME}/bin/catalina.sh"

mvn package
rm -rf "${TOMCAT_HOME}/webapps/${QUICKSTART_NAME}/"
cp target/${QUICKSTART_NAME}.war "${TOMCAT_HOME}/webapps/"
export JPDA_SUSPEND=n
"${TOMCAT_HOME}/bin/catalina.sh" jpda run &
if [ "$JPDA_SUSPEND" != "n" ]; then
  read -p "Press enter to continue" CONTINUE
else
  sleep `timeout_adjust 10 2>/dev/null || echo 10`
fi

for i in {1..10}
do
  curl ${CURL_IP_OPTS} --data "test$i" http://localhost:8080/${QUICKSTART_NAME}
done

curl ${CURL_IP_OPTS} http://localhost:8080/${QUICKSTART_NAME}

# remove all strings
curl ${CURL_IP_OPTS} -X DELETE http://localhost:8080/${QUICKSTART_NAME}

# crash the application
curl ${CURL_IP_OPTS} --data "crash" http://localhost:8080/${QUICKSTART_NAME}/crash

# restart the Tomcat
JPDA_SUSPEND=n "${TOMCAT_HOME}/bin/catalina.sh" jpda run &

# verify the recovery
if [ "$JPDA_SUSPEND" != "n" ]; then
  read -p "Press enter to continue" CONTINUE
else
  sleep `timeout_adjust 5 2>/dev/null || echo 5`
fi
recovery_response=`curl ${CURL_IP_OPTS} -s http://localhost:8080/${QUICKSTART_NAME}/recovery`

"${TOMCAT_HOME}/bin/catalina.sh" stop
rm -rf apache-tomcat-$TOMCAT_VERSION

if [ "${recovery_response}" != "[\"crash\"]" ]; then
    echo "Crash and Recovery failed, response on calling ${QUICKSTART_NAME}/recovery was: ${recovery_response}"
    exit -1
fi

echo "All tests succeeded"
