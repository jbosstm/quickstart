Integration with Agroal jdbc pooling library 
===

Author: Ondra Chaloupka<br/>
Level: Intermediate<br/>
Technologies: JTA, JDBC, Agroal


What is it?
---

This examle demostrates usage of Agroal (https://agroal.github.io) jdbc pooling library
in the standalone application while integrated with Narayana transaction manager.


Prerequisities
---

JDK8 required for the quickstart to run.


Overview
---

Agroal.io is a connection pool implementation. It's performant with a easy to use API.
This quickstart shows how to integrate this pooling library with Narayana
when you consider to use the power of XA transaction management.

The integration code you want to use in your application is presented at class
[src/main/java/io/narayana/AgroalDatasource.java](src/main/java/io/narayana/AgroalDatasource.java)

This quickstart works with H2 database and the functionality is tested at the test class
[src/test/java/io/narayana/AgroalTest.java](src/test/java/io/narayana/AgroalTest.java)

The settings of Narayana transaction manager can be checked at
[jbossts-properties.xml](src/main/resources/jbossts-properties.xml).
Please consider the comments which provides guidelines for each property. 


Testing
---

You can run tests and check the behaviour in case of commit, rollback and recovery scenarios just by

```
mvn test
```

To get information from the TRACE log for example to check how recovery works, you can run with added java util logging
properties file.

```
mvn test -Dtest=AgroalTest#commit \
  -Djava.util.logging.config.file=src/main/resources/logging.properties 2>&1 | tee my.log
```
