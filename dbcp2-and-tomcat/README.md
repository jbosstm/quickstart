### Description

This quickstart shows how to get started with Narayana and common-dbcp2 with a simple JDBC example.

### Start Tomcat

Start Tomcat in the usual manner, for example:
`$TOMCAT_HOME/bin/catalina.sh  run`

### Build the app

`mvn clean package`

### Deploy the app

`cp target/*.war apache-tomcat-7.0.78/webapps/`

### Get strings from the database

`curl http://localhost:8080/dbcp2-and-tomcat`

### Save string to the database

`curl --data "test" http://localhost:8080/dbcp2-and-tomcat`

### Crash and Recovery

`curl --data "crash" http://localhost:8080/dbcp2-and-tomcat/crash`

Restart Tomcat
`$TOMCAT_HOME/bin/catalina.sh  run`
`curl http://localhost:8080/dbcp2-and-tomcat/recovery`

