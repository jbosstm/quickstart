#/bin/bash
set -e

TOMCAT_VERSION=7.0.81
wget -nc http://www.mirrorservice.org/sites/ftp.apache.org/tomcat/tomcat-7/v$TOMCAT_VERSION/bin/apache-tomcat-$TOMCAT_VERSION.zip
rm -rf apache-tomcat-$TOMCAT_VERSION
unzip apache-tomcat-$TOMCAT_VERSION.zip
export TOMCAT_HOME=$(pwd)/apache-tomcat-$TOMCAT_VERSION/
echo "export JAVA_OPTS=\"-Dcom.arjuna.ats.jta.recovery.XAResourceRecovery1=com.arjuna.ats.internal.jdbc.recovery.BasicXARecovery\;abs://$(pwd)/src/main/resources/h2recoveryproperties.xml\ \;1\"" > $TOMCAT_HOME/bin/setenv.sh
chmod +x $TOMCAT_HOME/bin/catalina.sh
mvn package
rm -rf $TOMCAT_HOME/webapps/transactionaldriver-and-tomcat/
cp target/transactionaldriver-and-tomcat.war $TOMCAT_HOME/webapps/
JPDA_SUSPEND=n $TOMCAT_HOME/bin/catalina.sh jpda run &
sleep 10

for i in {1..10}
do
  curl -f --data "test$i" http://localhost:8080/transactionaldriver-and-tomcat
done

curl -f http://localhost:8080/transactionaldriver-and-tomcat
$TOMCAT_HOME/bin/catalina.sh stop