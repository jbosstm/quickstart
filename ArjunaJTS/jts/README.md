# Java Transaction Service - Distributed EJB Transactions Across Multiple Containers

<span>|<span>
:-----|:-----
Author:|Tom Jenkinson
Level:|Intermediate
Technologies:|JTS
Summary:|Uses Java Transaction Service (JTS) <br>to coordinate distributed transactions
Prerequisites:|cmt

## Pre-requisites

Developers should be familiar with the concepts introduced in the
[wildfly/quickstart:cmt](https://github.com/wildfly/quickstart/blob/master/cmt/) quickstart.

## What is it?

This example demonstrates how to perform distributed transactions in an application.
A distributed transaction is a set of operations performed by two or more nodes,
participating in an activity coordinated as a single entity of work,
and fulfilling the properties of an ACID transaction.

ACID is a set of 4 properties that guarantee the resources are processed in the following manner:

* Atomic - if any part of the transaction fails, all resources remain unchanged.
* Consistent - the state will be consistent across resources after a commit
* Isolated - the execution of the transaction for each resource is isolated from each others
* Durable - the data will persist after the transaction is committed


The example uses Java Transaction Service (JTS) to propagate a transaction context across two Container-Managed Transaction (CMT) EJBs that,
although deployed in separate servers, participate in the same transaction. In this example, one server processes the Customer and Account data
and the other server processes the Invoice data.

The code base is based on the [wildfly/quickstart:cmt](https://github.com/wildfly/quickstart/blob/master/cmt/)
while demonstrates the `InvoiceManager` used as distributed JTS transactions.
The code has been separated to a different deployment archive
to demonstrate the usage of JTS. The separated manager is in two classes

1. `application-component-2/src/main/java/org/jboss/as/quickstarts/cmt/jts/ejb/InvoiceManagerEJB`
2. `application-component-1/src/main/java/org/jboss/as/quickstarts/cmt/jts/ejb/CustomerManagerEJB.java`

You will see that the `CustomerManagerEJB` uses the EJB home for the remote EJB, this is expected to connect to remote EJBs.
The example expects the EJBs to be deployed onto the same physical machine. This is not a restriction of JTS
and the example can easily be converted to run on separate machines by editing the hostname value for the `InvoiceManagerEJB` in `org.jboss.as.quickstarts.cmt.jts.ejb.CustomerManagerEJB`.

A simple MDB has been provided that prints out the messages sent but this is not a transactional MDB and is purely provided for debugging purposes.

After users complete this quickstart, they are invited to run through the following quickstart:

1. [_wildfly/quickstart:jts-distributed-crash-rec_](https://github.com/wildfly/quickstart/tree/master/jts-distributed-crash-rec)
   - The crash recovery quickstart builds upon this quickstart by demonstrating the fault tolerance of WildFly.


## System requirements

All you need to build this project is Java 6.0 (Java SDK 1.6) or better, Maven 3.0 or better.

The application this project produces is designed to be run on WildFly (tested with WildFly 14).

## Configure Maven

If you have not yet done so, you must install Maven before testing the quickstarts.

## Configure the WildFly/JBoss servers

For this example, you will need two instances of the application server, with a subtle startup configuration difference.
Application server 2 must be started up with a port offset parameter provided to the startup script as `-Djboss.socket.binding.port-offset=100`

The application servers should both be configured to allow JTS transaction to work.
The best way to make changes in the WildFly application server is using `jboss-cli` commands.

1. Start the console
   ```
   $JBOSS_HOME/bin/jboss-cli.sh
   ```
   > NOTE: for Windows you need to run jboss-cli.bat`

2. Start the client in [embedded mode](https://access.redhat.com/documentation/en-us/red_hat_jboss_enterprise_application_platform/7.1/html/management_cli_guide/running_embedded_server)
   and define `standalone-full.xml` as the target configuration.
   ```
   embed-server --server-config=standalone-full.xml
   ```
   > NOTE: you can start the WildFly server and then connect the jboss-cli to the running
     application server but embedded mode offers ease of the configuration via script offline

3. Enable JTS as follows:
    * Enable JTS transaction support in IIOP subsystem
      ```
      /subsystem=iiop-openjdk:write-attribute(name=transactions, value=full)
      ```
    * Enable JTS in transaction subsystem  
      ```
      /subsystem=transactions:write-attribute(name=jts, value=true)
      ```

3. Duplicate of the WildFly distribution directory to use for the second server.

```
export JBOSS_HOME_SERVER_1="$JBOSS_HOME"
export JBOSS_HOME_SERVER_2="${JBOSS_HOME}-2"
cp -r "$JBOSS_HOME_SERVER_1" "$JBOSS_HOME_SERVER_2"
unset JBOSS_HOME
```

_IMPORTANT_: After you have finished with the quickstart, if you no longer wish to use JTS, it is important to restore your backup from step 1 above.


## Start the WildFly application server

If you are using Linux:

* Server 1: `$JBOSS_HOME_SERVER_1/bin/standalone.sh -c standalone-full.xml`
* Server 2: `$JBOSS_HOME_SERVER_2/bin/standalone.sh -c standalone-full.xml -Djboss.socket.binding.port-offset=100`

If you are using Windows

* Server 1: `%JBOSS_HOME_SERVER_1%\bin\standalone.bat -c standalone-full.xml`
* Server 2: `%JBOSS_HOME_SERVER_2%\bin\standalone.bat -c standalone-full.xml -Djboss.socket.binding.port-offset=100`


## Build and Deploy the Quickstart

Since this quickstart builds two separate components, you can not use the standard *Build and Deploy*
commands used by most of the other quickstarts. You must follow these steps to build, deploy, and run this quickstart.


1. Make sure you have started both WildFly servers
2. Open a command line and navigate to the [root directory of this quickstart](./).
3. Type this command to build and deploy the archive:

  ```
  mvn clean package wildfly:deploy
  ```

4. This will deploy war artifacts to the running instances of the WildFly application server

    * `application-component-1/target/jboss-as-jts-application-component-1.war` to `JBOSS_HOME_SERVER_1`
    * `application-component-2/target/jboss-as-jts-application-component-2.jar` to `JBOSS_HOME_SERVER_2`

## Access the application

The application will be running at the following URL: <http://localhost:8080/jboss-as-jts-application-component-1/>.
This will redirect you directly to page `addCustomer.jsf` where you are informed
about a customer was added.
By default hitting the page one customer is added. If you want to add more then use parameter `?name=<number>`:
http://localhost:8080/jboss-as-jts-application-component-1/addCustomer.jsf?name=5
Such query adds 5 customers and you will be informed that 5 invocations of remote
method was processed and how much time they took.


## Expected output

When you asked with query parameter `name=5` then 5 invocation of the remote EJB bean is expected.

In WildFly server.log console you can see on server 1

```
14:26:13,400 INFO  [org.jboss.ejb.client] (default task-1) JBoss EJB Client version 4.0.11.Final
14:26:13,416 INFO  [org.jboss.as.quickstarts.cmt.controller.CustomerManager] (default task-1) invocation count to be processed: 5, current time: Fri Sep 21 14:26:13 CEST 2018
14:26:14,743 INFO  [org.jboss.as.quickstarts.cmt.controller.CustomerManager] (default task-1) invocationCount 5 took: 1327 which is 265 per invocation
```

and on server 2

```
14:26:13,886 INFO  [org.jboss.ejb.client] (p: default-threadpool; w: Idle) JBoss EJB Client version 4.0.11.Final
14:26:14,004 INFO  [org.jboss.as.quickstarts.cmt.jts.ejb.InvoiceManagerEJBImpl] (p: default-threadpool; w: Idle) Invoice manager was invoked with name: customer number: 1, invocation count: 1
14:26:14,222 INFO  [org.jboss.as.quickstarts.cmt.jts.ejb.InvoiceManagerEJBImpl] (p: default-threadpool; w: Idle) Invoice manager was invoked with name: customer number: 2, invocation count: 2
14:26:14,394 INFO  [org.jboss.as.quickstarts.cmt.jts.ejb.InvoiceManagerEJBImpl] (p: default-threadpool; w: Idle) Invoice manager was invoked with name: customer number: 3, invocation count: 3
14:26:14,527 INFO  [org.jboss.as.quickstarts.cmt.jts.ejb.InvoiceManagerEJBImpl] (p: default-threadpool; w: Idle) Invoice manager was invoked with name: customer number: 4, invocation count: 4
14:26:14,654 INFO  [org.jboss.as.quickstarts.cmt.jts.ejb.InvoiceManagerEJBImpl] (p: default-threadpool; w: Idle) Invoice manager was invoked with name: customer number: 5, invocation count: 5
```

The web page will also change and show you the new list of customers.


## Undeploy the Archive

1. Make sure you have started the WildFly as described above.
2. Open a command line and navigate to the root directory of [this quickstart](./).
3. When you are finished testing, type this command to undeploy the archive:

```
mvn package wildfly:undeploy
```

## What just happened

By invoking the `addCustomer.jsf` page there was processed call to EJB bean
which starts a transaction. As transaction subsystem is configured to run JTS
the transaction will be managed by Narayana JTS implementation.
The bean is configured to call the other server. As JTS is command we need to
use EJB2 beans for cross server communication.
The transactional context is passed over the wire and received by the second server
where the method is marked with transaction attribute 'MANDATORY'. The context
of transaction was passed thus method is processed and call is returned back
to the server 1.
