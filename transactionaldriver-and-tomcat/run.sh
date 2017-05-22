#/bin/bash
set -e

wget -nc http://mirror.vorboss.net/apache/tomcat/tomcat-7/v7.0.78/bin/apache-tomcat-7.0.78.zip
rm -rf apache-tomcat-7.0.78
unzip apache-tomcat-7.0.78.zip
export TOMCAT_HOME=$(pwd)/apache-tomcat-7.0.78/
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