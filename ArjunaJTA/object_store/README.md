# Narayana standalone object store quickstart

## Overview

A transaction manager must store enough information such that it can guarantee recovery from failures.
This is achieved by persisting information in an Object Store. Various implementation are provided
to cater for various application requirements.

1. FileStoreExample shows how to change the store type to a file base store but in directory different from the default;
2. HornetqStoreExample shows how to use the Hornetq journal for transaction logging;
3. VolatileStoreExample shows how to use an unsafe (because it does not persist logs in the event of
   failures and therefore does not support recovery) in-memory log store implementation.
4. JDBCStoreExample shows how to use a database for persisting transaction logs. This example uses [H2](https://www.h2database.com/).

## Usage

```
mvn compile
./run.[sh|bat]
```
or to run individual tests using the maven java exec plugin:

```
mvn -e compile exec:java -Dexec.mainClass=org.jboss.narayana.jta.quickstarts.VolatileStoreExample
mvn -e compile exec:java -Dexec.mainClass=org.jboss.narayana.jta.quickstarts.HornetqStoreExample
mvn -e compile exec:java -Dexec.mainClass=org.jboss.narayana.jta.quickstarts.FileStoreExample
mvn -e compile exec:java -Dexec.mainClass=org.jboss.narayana.jta.quickstarts.JDBCStoreExample
```

The JDBCStore can be configured programmatically or via a properties file. To use the properties file pass the argument `-DUSE_JBOSSTS_PROPERTIES=true` on the command line.

## Expected output

When running examples one at a time look for the output

```
[INFO] BUILD SUCCESS
```

If you use the run script then you the line "[INFO] BUILD SUCCESS" should appear once for each example.

## What just happened

Each example either changes the object store directory or object store type (or both) and then runs a
transaction. Each example performs a relevant test to verify that the object store type or directory,
as appropriate, was used.

## Viewing JDBC store logs

The example uses H2 so you could use the H2 console:

mvn dependency:copy-dependencies # download the H2 jar
java -cp target/dependency/h2-1.4.195.jar org.h2.tools.Server -tcp -web # start the H2 console
Now use a browser to navigate to the H2 console: http://192.168.0.14:8082/ and enter the appropriate connection parameters:

```
Driver Class: `org.h2.Driver`
JDBC URL: `jdbc:h2:~/<quickstart home>/ArjunaJTA/object_store/target/h2/JBTMDB`
User Name: `sa`
Password: `sa`
```

Once connected you may view pending transactions by running the following SQL query: `SELECT * FROM JBOSSTSTXTABLE`
The table is likely to be empty unless the quickstart crashes between the prepare and commit calls (which you can arrange by either using a IDE with suitable breakpoints or by changing the `DummyXAResource.java` source file to halt the JVM during the commit call).

Note that H2 restricts the number of connections so if you have the console open you can't run the quickstart and vice versa.
