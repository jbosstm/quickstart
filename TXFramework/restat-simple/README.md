restat-simple: REST-AT (REST Atomic Transaction) - Simple, using the TXFramework
================================================================================
Author: Paul Robinson

_NOTE: This quickstart utilises technology that is in status "Tech Preview". Although the quickstart is tested regularly,
you may find issues when developing your own applications using the TXFramework. We are very keen to hear about your
experiences with the TXFramework and of any bugs you may find. Please direct these to the JBossTS forum.

What is it?
-----------

This example demonstrates the deployment of a REST-AT (RESTful AtomicTransaction) enabled JAX-WS Web service bundled in a WAR
archive for deployment to *JBoss AS 7*.

The example uses the annotation support provided by the TXFramework. The TXFramwork provides annotation support for
writing transactional applications. This particular example shows how it can be used to write a REST-AT application.
However, the TXFramework provides support for other Transaction protocols, such as WS-AT and WS-BA.
See the other TXFramework quickstarts for more examples and the README.md file in the directory above, for a more complete
description of what the TXFramework provides.

In particular this example showcases the following features of the TXFramework:

1. Annotation support for developing participants. Traditionally a separate REST Service needed to be developed that responded to protocol events. With the TXFramework the developer can simply annotate methods of the application that participate in the transaction protocol. For example, a method annotated by @Commit will be invoked at the commit phase of the protocol.
2. Automatic participant registration. A participant is automatically registered with the transaction. Traditionally, the application needed to do this registration manually.
3. Per-participant data-management. This allows the application to store data in a map that is tied to the transaction participant. When transaction lifecycle methods are invoked, the data for just that transaction is made available through this map.


The quickstart comprises of a Web service that is offered by a Restaurant for making bookings. The Service allows bookings to be made within an Atomic Transaction.

This example demonstrates the basics of implementing a REST-AT enabled Web service. It is beyond the scope of this quick start to demonstrate more advanced features. In particular:

1. The Service does not implement the required hooks to support recovery in the presence of failures.
2. It also does not utilize a transactional back end resource.

For a more complete example, please see the REST-AT demonstrator application and quickstarts that ship with the JBossTS project: http://www.jboss.org/jbosstm. However, this does not use the TXFramework.

It is also assumed that you have an understanding of REST-AT. For more details, read the RTS documentation
that ships with the Narayana project, which can be downloaded here: http://www.jboss.org/jbosstm/downloads/.
Please not that this documentation focuses on developing applications *without* the TXFramework.

The application consists of two JAX-RS web services that are deployed within a WAR archive. It is tested with a JBoss Arquillian enabled JUnit test.

When running the `org.jboss.narayana.quickstarts.restat#clientDrivenCommitTest()` method, the following steps occur:

1. The REST-AT coordinator is invoked to begin a new REST-AT transaction (AT).
2. An operation on a REST-AT enabled service is invoked by the client.
3. The client side interceptor inserts the transaction URL into the outgoing request.
4. When the service receives the request, an interceptor inspects the REST-AT transaction url and enlists a participant with the REST-AT coordinator. This allows the Web Service logic to respond to protocol events, such as Commit and Rollback, via the @Prepare, @Commit and @Rollback annotated methods on the service.
5. The service operation is invoked. In this case, a booking is made with the restaurant.
6. The client can then decide to commit or rollback the AT. If the client decides to commit, the coordinator will begin the 2PC protocol. If the participant decides to rollback, all participants will be told to rollback.

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

Next you need to start the JBoss AS 7 server that you just built, with the XTS sub system enabled. This is enabled through the optional server configuration *standalone-xts.xml*. To do this, run the following commands from the top-level directory of JBossAS:

        For Linux:     ./bin/standalone.sh --server-config=../../docs/examples/configs/standalone-xts.xml
        For Windows:   \bin\standalone.bat --server-config=..\..\docs\examples\configs\standalone-xts.xml


Run the Arquillian Tests 
-------------------------

This quickstart provides Arquillian tests. By default, these tests are configured to be skipped as Arquillian tests require the use of a container. 

1. Make sure you have started the JBoss Server as described above.
2. Deploy the REST-AT coordinator:

        cp <Narayana source directory>/rest-tx/webservice/target/rest-tx-web-5.0.0.M2-SNAPSHOT.war $JBOSS_HOME/standalone/deployments/

3. Open a command line and navigate to the root directory of this quickstart.
4. Type the following command to run the test goal with the following profile activated:

        mvn clean test -Parq-jbossas-remote 


Investigate the Console Output
----------------------------

The following expected output should appear. Note there will be some other log messages interlaced between these. The output explains what actually went on when these tests ran.

Test commit:

    19:59:15,020 INFO  [stdout] (http-localhost/127.0.0.1:8080-2) [SERVICE] Restaurant service invoked to make a booking
    19:59:15,020 INFO  [stdout] (http-localhost/127.0.0.1:8080-2) [SERVICE] Invoking the back-end business logic
    19:59:15,020 INFO  [stdout] (http-localhost/127.0.0.1:8080-2) [SERVICE] makeBooking called on backend resource.
    19:59:15,041 INFO  [stdout] (http-localhost/127.0.0.1:8080-3) Service: Enlisting terminator=http://localhost:8080/test/taxi0_ffffc0a80068_2a61b0f5_4fe21d66_20/1/terminate;durableparticipant=http://localhost:8080/test/taxi0_ffffc0a80068_2a61b0f5_4fe21d66_20/1/terminator
    19:59:15,044 INFO  [stdout] (http-localhost/127.0.0.1:8080-3) [SERVICE] taxi service invoked to make a booking
    19:59:15,044 INFO  [stdout] (http-localhost/127.0.0.1:8080-3) [SERVICE] Invoking the back-end business logic
    19:59:15,045 INFO  [stdout] (http-localhost/127.0.0.1:8080-3) [SERVICE] makeBooking called on backend resource.
    19:59:15,053 INFO  [stdout] (http-localhost/127.0.0.1:8080-4) [SERVICE] Prepare called on participant, about to prepare the back-end resource
    19:59:15,053 INFO  [stdout] (http-localhost/127.0.0.1:8080-4) [SERVICE] prepare called on backend resource.
    19:59:15,053 INFO  [stdout] (http-localhost/127.0.0.1:8080-4) [SERVICE] back-end resource prepared, participant votes prepared
    19:59:15,061 INFO  [stdout] (http-localhost/127.0.0.1:8080-4) [SERVICE] Prepare called on participant, about to prepare the back-end resource
    19:59:15,061 INFO  [stdout] (http-localhost/127.0.0.1:8080-4) [SERVICE] prepare called on backend resource.
    19:59:15,061 INFO  [stdout] (http-localhost/127.0.0.1:8080-4) [SERVICE] back-end resource prepared, participant votes prepared
    19:59:15,065 INFO  [stdout] (http-localhost/127.0.0.1:8080-4) [SERVICE] all participants voted 'prepared', so coordinator tells the participant to commit
    19:59:15,065 INFO  [stdout] (http-localhost/127.0.0.1:8080-4) [SERVICE] commit called on backend resource.
    19:59:15,067 INFO  [stdout] (http-localhost/127.0.0.1:8080-4) [SERVICE] all participants voted 'prepared', so coordinator tells the participant to commit
    19:59:15,067 INFO  [stdout] (http-localhost/127.0.0.1:8080-4) [SERVICE] commit called on backend resource.
    19:59:15,183 INFO  [stdout] (http-localhost/127.0.0.1:8080-5) Service: Enlisting terminator=http://localhost:8080/test/restaurant0_ffffc0a80068_2a61b0f5_4fe21d66_2b/2/terminate;durableparticipant=http://localhost:8080/test/restaurant0_ffffc0a80068_2a61b0f5_4fe21d66_2b/2/terminator
    19:59:15,186 INFO  [stdout] (http-localhost/127.0.0.1:8080-5) [SERVICE] Restaurant service invoked to make a booking
    19:59:15,186 INFO  [stdout] (http-localhost/127.0.0.1:8080-5) [SERVICE] Invoking the back-end business logic
    19:59:15,187 INFO  [stdout] (http-localhost/127.0.0.1:8080-5) [SERVICE] makeBooking called on backend resource.
    19:59:15,200 INFO  [stdout] (http-localhost/127.0.0.1:8080-6) Service: Enlisting terminator=http://localhost:8080/test/taxi0_ffffc0a80068_2a61b0f5_4fe21d66_2b/3/terminate;durableparticipant=http://localhost:8080/test/taxi0_ffffc0a80068_2a61b0f5_4fe21d66_2b/3/terminator
    19:59:15,203 INFO  [stdout] (http-localhost/127.0.0.1:8080-6) [SERVICE] taxi service invoked to make a booking
    19:59:15,203 INFO  [stdout] (http-localhost/127.0.0.1:8080-6) [SERVICE] Invoking the back-end business logic
    19:59:15,205 INFO  [stdout] (http-localhost/127.0.0.1:8080-6) [SERVICE] makeBooking called on backend resource.
    19:59:15,209 INFO  [stdout] (http-localhost/127.0.0.1:8080-4) [SERVICE] one or more participants voted 'aborted' or a failure occurred, so coordinator tells the participant to rollback
    19:59:15,209 INFO  [stdout] (http-localhost/127.0.0.1:8080-4) [SERVICE] rollback called on backend resource.
    19:59:15,212 INFO  [stdout] (http-localhost/127.0.0.1:8080-4) [SERVICE] one or more participants voted 'aborted' or a failure occurred, so coordinator tells the participant to rollback
    19:59:15,212 INFO  [stdout] (http-localhost/127.0.0.1:8080-4) [SERVICE] rollback called on backend resource.

Test rollback:

    19:59:15,183 INFO  [stdout] (http-localhost/127.0.0.1:8080-5) Service: Enlisting terminator=http://localhost:8080/test/restaurant0_ffffc0a80068_2a61b0f5_4fe21d66_2b/2/terminate;durableparticipant=http://localhost:8080/test/restaurant0_ffffc0a80068_2a61b0f5_4fe21d66_2b/2/terminator
    19:59:15,186 INFO  [stdout] (http-localhost/127.0.0.1:8080-5) [SERVICE] Restaurant service invoked to make a booking
    19:59:15,186 INFO  [stdout] (http-localhost/127.0.0.1:8080-5) [SERVICE] Invoking the back-end business logic
    19:59:15,187 INFO  [stdout] (http-localhost/127.0.0.1:8080-5) [SERVICE] makeBooking called on backend resource.
    19:59:15,200 INFO  [stdout] (http-localhost/127.0.0.1:8080-6) Service: Enlisting terminator=http://localhost:8080/test/taxi0_ffffc0a80068_2a61b0f5_4fe21d66_2b/3/terminate;durableparticipant=http://localhost:8080/test/taxi0_ffffc0a80068_2a61b0f5_4fe21d66_2b/3/terminator
    19:59:15,203 INFO  [stdout] (http-localhost/127.0.0.1:8080-6) [SERVICE] taxi service invoked to make a booking
    19:59:15,203 INFO  [stdout] (http-localhost/127.0.0.1:8080-6) [SERVICE] Invoking the back-end business logic
    19:59:15,205 INFO  [stdout] (http-localhost/127.0.0.1:8080-6) [SERVICE] makeBooking called on backend resource.
    19:59:15,209 INFO  [stdout] (http-localhost/127.0.0.1:8080-4) [SERVICE] one or more participants voted 'aborted' or a failure occurred, so coordinator tells the participant to rollback
    19:59:15,209 INFO  [stdout] (http-localhost/127.0.0.1:8080-4) [SERVICE] rollback called on backend resource.
    19:59:15,212 INFO  [stdout] (http-localhost/127.0.0.1:8080-4) [SERVICE] one or more participants voted 'aborted' or a failure occurred, so coordinator tells the participant to rollback
    19:59:15,212 INFO  [stdout] (http-localhost/127.0.0.1:8080-4) [SERVICE] rollback called on backend resource.
