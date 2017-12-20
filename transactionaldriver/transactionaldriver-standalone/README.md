JDBC tranactional driver at standalone application
===

Author: Ondra Chaloupka
Level: Intermediate
Technologies: JTA, JDBC


What is it?
---

This examle demostrates usage of jdbc transactional driver in a standalone application.


Prerequisities
---

JDK8 expected.


Overview
---

You can spot here three different approaches to work with Narayana jdbc transactional driver
(the implementation can be found at https://github.com/jbosstm/narayana/tree/master/ArjunaJTA/jdbc).

You can see here how to register an XADataSource to the driver, how to get managed connection and then how to work
with such connection to be correctly handled in Narayana. Additionally you can check settings needed for
recovery works.

See the approaches at classes

* [src/main/java/io/narayana/DriverProvidedXADataSource.java](src/main/java/io/narayana/DriverProvidedXADataSource.java)
* [src/main/java/io/narayana/DriverDirectRecoverable.java](src/main/java/io/narayana/DriverDirectRecoverable.java)
* [src/main/java/io/narayana/DriverIndirectRecoverable.java](src/main/java/io/narayana/DriverIndirectRecoverable.java)

The settings of recovery could be seen in recovery util class
(_Recovery settings is moved  out of the connection showcases as it's independent
on connection settings and is expected that will be done once globally for the whole application._)

* [src/main/java/io/narayana/recovery/RecoverySetupUtil.java](src/main/java/io/narayana/recovery/RecoverySetupUtil.java)


Testing
---

You can run tests and check the behaviour in case of commit, rollback and recovery scenarios just by

```
mvn test
```

To get information from the TRACE log for example to check how recovery works, you can run with added java util logging
properties file.

```
mvn test -Dtest=TransactionalDriverTest#transactionDriverDirectRecoverableRecovery\
  -Djava.util.logging.config.file=src/main/resources/logging.properties 2>&1 | tee my.log
```

Running with PostgreSQL
---

The tests are prepared are run with H2 database. If you are interested in setup for PostgreSQL
database you need to manually(!) change the code.
Or in case you can find inspiration of settings needed for PostgreSQL here.

The steps to run with PostgreSQL are:

1. go to class `DBUtils.java` and change
    * variable `DB_USER` to value of `DB_PG_USER`
    * variable `DB_PASSWORD` to value of `DB_PG_PASSWORD`
    * method `getConnection` will call `getPgConnection`
    * method `getXADatasource` will call `getPgXADatasource`
2. go to class `DriverDirectRecoverable.java` and change
    * variable `jdbcUrl1` to use value of properties file string `ds1.pg.properties`
    * variable `jdbcUrl2` to use value of properties file string `ds2.pg.properties`
3. go to descriptor file `recovery-basicxa-test1.xml` and change the user and password appropriately
4. go to descriptor file `recovery-jdbcxa-test1.xml` and change the user and password appropriately
