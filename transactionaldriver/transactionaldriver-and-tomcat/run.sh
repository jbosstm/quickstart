#/bin/bash
set -e

export QUICKSTART_NAME=${PWD##*/}
TOMCAT_VERSION=9.0.7
wget -nc https://archive.apache.org/dist/tomcat/tomcat-9/v$TOMCAT_VERSION/bin/apache-tomcat-$TOMCAT_VERSION.zip
rm -rf apache-tomcat-$TOMCAT_VERSION
unzip apache-tomcat-$TOMCAT_VERSION.zip
cp ~/.m2/repository/org/jboss/spec/javax/transaction/jboss-transaction-api_1.2_spec/1.0.0.Final/jboss-transaction-api_1.2_spec-1.0.0.Final.jar apache-tomcat-$TOMCAT_VERSION/lib/
export TOMCAT_HOME=$(pwd)/apache-tomcat-$TOMCAT_VERSION/
echo "export JAVA_OPTS=\"-Dcom.arjuna.ats.jta.recovery.XAResourceRecovery1=com.arjuna.ats.internal.jdbc.recovery.BasicXARecovery\;abs://$(pwd)/src/main/resources/h2recoveryproperties.xml\ \;1\"" > $TOMCAT_HOME/bin/setenv.sh
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
$TOMCAT_HOME/bin/catalina.sh stop
rm -rf apache-tomcat-$TOMCAT_VERSION
