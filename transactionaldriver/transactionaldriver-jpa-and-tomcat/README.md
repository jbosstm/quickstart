### Description

This quickstart shows how to get started with Narayana and Transactional Driver with a JPA example.

The quickstart also demonstrates the use of connection pooling features provided by commons-dbcp.

The following steps show how to execute the quickstart by hand. You can also execute the run.sh script (in shell environments) to follow the precise steps we test with.

### Start Tomcat

You must add a $TOMCAT_HOME/bin/setenv.sh with the following content:
`export JAVA_OPTS="-Dcom.arjuna.ats.jta.recovery.XAResourceRecovery1=com.arjuna.ats.internal.jdbc.recovery.BasicXARecovery\;abs://$QUICKSTART_HOME/src/main/resources/h2recoveryproperties.xml\ \;1"`

Start Tomcat in the usual manner, for example:
`$TOMCAT_HOME/bin/catalina.sh  run`

### Build the app

`mvn clean package`

### Deploy the app

`cp target/*.war $TOMCAT_HOME/webapps/`

### Get strings from the database

`curl http://localhost:8080/transactionaldriver-jpa-and-tomcat`

### Save string to the database

`curl --data "test" http://localhost:8080/transactionaldriver-jpa-and-tomcat`
