wsat-simple: WS-AT (WS-AtomicTransaction) - Simple using the TXFramework
========================================================================
Author: Paul Robinson

_NOTE: This quickstart utilises technology that is in status "Tech Preview". Although the quickstart is tested regularly,
you may find issues when developing your own applications using the TXFramework. We are very keen to hear about your
experiences with the TXFramework and of any bugs you may find. Please direct these to the JBossTS forum.

What is it?
-----------

This example demonstrates the deployment of a WS-AT (WS-AtomicTransaction) enabled JAX-WS Web service bundled in a WAR
archive for deployment to  *JBoss Enterprise Application Platform 6* or *JBoss AS 7*..

The example uses the annotation support provided by the TXFramework. The TXFramwork provides annotation support for
writing transactional applications. This particular example shows how it can be used to write a WS-AT application.
However, the TXFramework provides support for other Transaction protocols, such as REST-AT and WS-BA.
See the other TXFramework quickstarts for more examples and the README.md file in the directory above, for a more complete
description of what the TXFramework provides.

In particular this example showcases the following features of the TXFramework:

1. Annotation support for developing participants. Traditionally a separate participant class needed to be developed that implemented (all methods) of a particular interface. With the TXFramework the developer can simply annotate methods of the application, that participate in the transaction protocol. For example, a method annotated by @Commit will be invoked at the commit phase of the protocol.
2. Automatic participant registration. A participant is automatically registered with the transaction. Traditionally, the application needed to do this registration manually.
3. Per-participant data-management. This allows the application to store data in a map that is tied to the transaction participant. When transaction lifecycle methods are invoked, the data for just that transaction is made available through this map.


The quickstart comprises of a Web service that is offered by a Restaurant for making bookings. The Service allows bookings to be made within an Atomic Transaction.

This example demonstrates the basics of implementing a WS-AT enabled Web service. It is beyond the scope of this quick start to demonstrate more advanced features. In particular:

1. The Service does not implement the required hooks to support recovery in the presence of failures.
2. It also does not utilize a transactional back end resource.
3. Only one Web service participates in the protocol. As WS-AT is a 2PC coordination protocol, it is best suited to multi-participant scenarios.

For a more complete example, please see the XTS demonstrator application that ships with the JBossTS project: http://www.jboss.org/jbosstm. However, this does not use the TXFramework.

It is also assumed that you have an understanding of WS-AtomicTransaction. For more details, read the XTS documentation
that ships with the Narayana project, which can be downloaded here: http://www.jboss.org/jbosstm/downloads/JBOSSTS_4_16_0_Final.
Please not that this documentation focuses on developing applications *without* the TXFramework.

The application consists of a single JAX-WS web service that is deployed within a WAR archive. It is tested with a JBoss Arquillian enabled JUnit test.

When running the `org.jboss.as.quickstarts.wsat.simple.ClientTest#testCommit()` method, the following steps occur:

1. A new Atomic Transaction (AT) is created by the client.
2. An operation on a WS-AT enabled Web service is invoked by the client.
3. The JaxWSHeaderContextProcessor in the WS Client handler chain inserts the WS-AT context into the outgoing SOAP message
4. When the service receives the SOAP request, the JaxWSHeaderContextProcessor in its handler chain inspects the WS-AT context and associates the request with this AT. This handler is automatically added by the TXFramework.
5. The Web service operation is invoked...
6. The TXFramework enlists a participant in this AT. This allows the Web Service logic to respond to protocol events, such as Commit and Rollback, via the @Prepare, @Commit and @Rollback annotated methods on the service.
7. The service invokes the business logic. In this case, a booking is made with the restaurant.
8. The backend resource is prepared. This ensures that the Backend resource can undo or make permanent the change when told to do so by the coordinator.
10. The client can then decide to commit or rollback the AT. If the client decides to commit, the coordinator will begin the 2PC protocol. If the participant decides to rollback, all participants will be told to rollback.

There is another test that shows what happens if the client decides to rollback the AT.


System requirements
-------------------

All you need to build this project is Java 6.0 (Java SDK 1.6) or better, Maven 3.0 or better.

_NOTE: The TXFramework does not currently ship with JBossAS. This is because it is currently a tech preview.
You will need to follow the instructions in the README.md file found in the parent directory to this quickstart.


Start JBoss AS 7 with the Custom Options
----------------------

First, edit the log level to reduce the amount of log output. This should make it easier to read the logs produced by this example. To do this add the
following logger block to the ./docs/examples/configs/standalone-xts.xml of your JBoss distribution. You should add it just bellow one of the other logger blocks.

        <logger category="org.apache.cxf.service.factory.ReflectionServiceFactoryBean">
            <level name="WARN"/>
        </logger>         

Next you need to start JBoss Enterprise Application Platform 6 or JBoss AS 7 (7.1.0.CR1 or above), with the XTS sub system enabled. This is enabled through the optional server configuration *standalone-xts.xml*. To do this, run the following commands from the top-level directory of JBossAS:

        For Linux:     ./bin/standalone.sh --server-config=../../docs/examples/configs/standalone-xts.xml
        For Windows:   \bin\standalone.bat --server-config=..\..\docs\examples\configs\standalone-xts.xml


Run the Arquillian Tests 
-------------------------

This quickstart provides Arquillian tests. By default, these tests are configured to be skipped as Arquillian tests require the use of a container. 

1. Make sure you have started the JBoss Server as described above.
2. Open a command line and navigate to the root directory of this quickstart.
3. Type the following command to run the test goal with the following profile activated:

        mvn clean test -Parq-jbossas-remote 


Investigate the Console Output
----------------------------

The following expected output should appear. Note there will be some other log messages interlaced between these. The output explains what actually went on when these tests ran.

Test commit:

    14:06:28,208 INFO  [stdout] (management-handler-threads - 14) Starting 'testCommit'. This test invokes a WS within an AT. The AT is later committed, which causes the back-end resource(s) to be committed.
    14:06:28,209 INFO  [stdout] (management-handler-threads - 14) [CLIENT] Creating a new WS-AT User Transaction
    14:06:28,209 INFO  [stdout] (management-handler-threads - 14) [CLIENT] Beginning Atomic Transaction (All calls to Web services that support WS-AT wil be included in this transaction)
    14:06:28,532 INFO  [stdout] (management-handler-threads - 14) [CLIENT] invoking makeBooking() on WS
    14:06:29,168 INFO  [stdout] (http-localhost-127.0.0.1-8080-1) [SERVICE] Restaurant service invoked to make a booking
    14:06:29,168 INFO  [stdout] (http-localhost-127.0.0.1-8080-1) [SERVICE] Enlisting a Durable2PC participant into the AT
    14:06:29,410 INFO  [stdout] (http-localhost-127.0.0.1-8080-1) [SERVICE] Invoking the back-end business logic
    14:06:29,410 INFO  [stdout] (http-localhost-127.0.0.1-8080-1) [SERVICE] makeBooking called on backend resource.
    14:06:29,411 INFO  [stdout] (management-handler-threads - 14) [CLIENT] committing Atomic Transaction (This will cause the AT to complete successfully)
    14:06:29,974 INFO  [stdout] (TaskWorker-3) [SERVICE] Prepare called on participant, about to prepare the back-end resource
    14:06:29,974 INFO  [stdout] (TaskWorker-3) [SERVICE] prepare called on backend resource.
    14:06:29,974 INFO  [stdout] (TaskWorker-3) [SERVICE] back-end resource prepared, participant votes prepared
    14:06:30,560 INFO  [stdout] (TaskWorker-3) [SERVICE] all participants voted 'prepared', so coordinator tells the participant to commit
    14:06:30,560 INFO  [stdout] (TaskWorker-3) [SERVICE] commit called on backend resource.

Test rollback:

    14:06:31,163 INFO  [stdout] (management-handler-threads - 13) Starting 'testRollback'. This test invokes a WS within an AT. The AT is later rolled back, which causes the back-end resource(s) to be rolled back.
    14:06:31,163 INFO  [stdout] (management-handler-threads - 13) [CLIENT] Creating a new WS-AT User Transaction
    14:06:31,164 INFO  [stdout] (management-handler-threads - 13) [CLIENT] Beginning Atomic Transaction (All calls to Web services that support WS-AT wil be included in this transaction)
    14:06:31,461 INFO  [stdout] (management-handler-threads - 13) [CLIENT] invoking makeBooking() on WS
    14:06:32,094 INFO  [stdout] (http-localhost-127.0.0.1-8080-1) [SERVICE] Restaurant service invoked to make a booking
    14:06:32,094 INFO  [stdout] (http-localhost-127.0.0.1-8080-1) [SERVICE] Enlisting a Durable2PC participant into the AT
    14:06:32,297 INFO  [stdout] (http-localhost-127.0.0.1-8080-1) [SERVICE] Invoking the back-end business logic
    14:06:32,322 INFO  [stdout] (http-localhost-127.0.0.1-8080-1) [SERVICE] makeBooking called on backend resource.
    14:06:32,324 INFO  [stdout] (management-handler-threads - 13) [CLIENT] rolling back Atomic Transaction (This will cause the AT and thus the enlisted back-end resources to rollback)
    14:06:32,818 INFO  [stdout] (TaskWorker-1) [SERVICE] one or more participants voted 'aborted' or a failure occurred, so coordinator tells the participant to rollback
    14:06:32,818 INFO  [stdout] (TaskWorker-1) [SERVICE] rollback called on backend resource.
