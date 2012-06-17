wsat-jta-bridge: WS-AT (WS-AtomicTransaction) - Simple, using the TXFramework
========================================================================
Author: Paul Robinson

_NOTE: This quickstart utilises technology that is in status "Tech Preview". Although the quickstart is tested regularly,
you may find issues when developing your own applications using the TXFramework. We are very keen to hear about your
experiences with the TXFramework and of any bugs you may find. Please direct these to the JBossTS forum.

What is it?
-----------

This example demonstrates the deployment of a WS-AT (WS-AtomicTransaction) enabled JAX-WS Web service bundled in a JAR archive for deployment to  *JBoss AS 7* that bridges to and from a JTA transaction.

The example uses the annotation support provided by the TXFramework. The TXFramwork provides annotation support for
writing transactional applications. This particular example shows how it can be used to write a WS-AT application that bridges from and to a JTA transaction.
However, the TXFramework provides support for other Transaction protocols, such as REST-AT and WS-BA.
See the other TXFramework quickstarts for more examples and the README.md file in the directory above, for a more complete
description of what the TXFramework provides.

In particular this example showcases the following features of the TXFramework:

1. Bridging a transaction from a JTA transaction to a WS-AT transaction. This bridging is not yet setup automatically by the TXFramework.
2. Bridging a transaction from a WS-AT transaction to a JTA transaction. This bridging is setup automatically by the TXFramework.

The quickstart comprises of a Web service that is offered by a Restaurant for making bookings. The Service allows bookings to be made within an Atomic Transaction.

This example demonstrates the basics of implementing a WS-AT enabled Web service. It is beyond the scope of this quick start to demonstrate more advanced features. In particular:

1. The Service does not implement the required hooks to support recovery in the presence of failures.
2. Only one Web service participates in the protocol. As WS-AT is a 2PC coordination protocol, it is best suited to multi-participant scenarios.

For a more complete example, please see the XTS demonstrator application that ships with the JBossTS project: http://www.jboss.org/jbosstm. However, this does not use the TXFramework.

It is also assumed that you have an understanding of WS-AtomicTransaction and JTA. For more details, read the XTS and JTA documentation
that ships with the Narayana project, which can be downloaded here: http://www.jboss.org/jbosstm/downloads/.
Please note that this documentation focuses on developing applications *without* the TXFramework.

The application consists of a single JAX-WS web service that is deployed within a JAR archive. It is tested with JBoss Arquillian enabled JUnit tests.

The quickstart offers two tests; `org.jboss.narayana.quickstarts.wsat.jtabridge.tojta.BridgeToJTATest` and `org.jboss.narayana.quickstarts.wsat.jtabridge.fromjta.BridgeFromJTATest`. The first test (BridgeToJTATest)
begins a WS-AT transaction on the client side and then bridges it on the server side to a JTA transaction. The second test (BridgeFromJTATest) begins a JTA transaction on the client side and then
bridges it to a WS-AT transaction for the Web service call. The incoming WS-AT transaction is then bridged on the server side to another JTA transaction.

When running the `org.jboss.narayana.quickstarts.wsat.jtabridge.tojta.BridgeToJTATest#testCommit()`, the following steps occur:

1. A new Atomic Transaction (WS-AT) is created by the client.
2. An operation on a WS-AT enabled Web service is invoked by the client.
3. The JaxWSHeaderContextProcessor in the WS Client handler chain inserts the WS-AT context into the outgoing SOAP message
4. When the service receives the SOAP request, the JaxWSHeaderContextProcessor in its handler chain inspects the WS-AT context and associates the request with this AT. This handler is automatically added by the TXFramework.
5. Next, the JaxWSTxInboundBridgeHandler in its handler chain starts a new JTA transaction and bridges it onto the incoming WS-AT transaction. This handler is automatically added by the TXFramework.
6. The Web service operation is invoked...
7. The service invokes the business logic. In this case, a booking is made with the restaurant, by looking up a JPA entity and updating the booking count
8. The client can then decide to commit or rollback the AT. If the client decides to commit, the coordinator will begin the 2PC protocol, which will be bridged onto the JTA transaction. If the participant decides to rollback, all participants will be told to rollback.

There is another test that shows what happens if the client decides to rollback the AT.

When running the `org.jboss.narayana.quickstarts.wsat.jtabridge.fromjta.BridgeFromJTATest#testCommit()`, the following steps occur:

1. A new JTA Transaction is created by the client.
2. An operation on a WS-AT enabled Web service is invoked by the client.
3. The JaxWSTxOutboundBridgeHandler in the WS Client handler chain starts a new WS-AT transaction and bridges to it from the JTA transaction.
3. The next handler, (JaxWSHeaderContextProcessor) in the WS Client handler chain inserts the WS-AT context into the outgoing SOAP message
4. When the service receives the SOAP request, the JaxWSHeaderContextProcessor in its handler chain inspects the WS-AT context and associates the request with this AT. This handler is automatically added by the TXFramework.
5. Next, the JaxWSTxInboundBridgeHandler in its handler chain starts a new JTA transaction and bridges it onto the incoming WS-AT transaction. This handler is automatically added by the TXFramework.
6. The Web service operation is invoked...
7. The service invokes the business logic. In this case, a booking is made with the restaurant, by looking up a JPA entity and updating the booking count
8. The client can then decide to commit or rollback the JTA transaction. If the client decides to commit, the coordinator will begin the 2PC protocol, which will be bridged from the JTA transaction on the client, to the WS-AT transaction and then to the other JTA transaction on the server. If the participant decides to rollback, all participants will be told to rollback.

There is another test that shows what happens if the client decides to rollback the JTA transaction.

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

        For Linux:     ./bin/standalone.sh --server-config=../../docs/examples/configs/standalone-xts.xml | egrep "stdout|stderr|started"
        For Windows:   \bin\standalone.bat --server-config=..\..\docs\examples\configs\standalone-xts.xml

Note the pipe to `egrep` on the linux command. this filters the output from the server and makes it easier to view the expected output.

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

BridgeToJTATest
---------------

Test commit:

    11:54:48,263 INFO  [stdout] (http-localhost-127.0.0.1-8080-1) [CLIENT] Beginning the first WS-AT transaction
    11:54:49,361 INFO  [stdout] (http-localhost-127.0.0.1-8080-1) [CLIENT] Calling makeBooking on the WS client stub.
    11:54:49,848 INFO  [stdout] (http-localhost-127.0.0.1-8080-3) [SERVICE] Restaurant service invoked to make a booking for '1'
    11:54:49,849 INFO  [stdout] (http-localhost-127.0.0.1-8080-3) [SERVICE] Using the JPA Entity Manager to update the BookingCountEntity within a JTA transaction
    11:54:49,957 INFO  [stdout] (http-localhost-127.0.0.1-8080-1) [CLIENT] Booking successful, about to commit the WS-AT transaction.
    11:54:51,688 INFO  [stdout] (http-localhost-127.0.0.1-8080-1) [CLIENT] Beginning the second WS-AT transaction
    11:54:51,765 INFO  [stdout] (http-localhost-127.0.0.1-8080-1) [CLIENT] Calling getBookingCount on the WS client stub.
    11:54:51,880 INFO  [stdout] (http-localhost-127.0.0.1-8080-3) [SERVICE] getBookingCount() invoked
    11:54:51,887 INFO  [stdout] (http-localhost-127.0.0.1-8080-1) [CLIENT] Booking count obtained successfully, about to commit the WS-AT transaction.
    11:54:52,398 INFO  [stdout] (http-localhost-127.0.0.1-8080-1) [CLIENT] Asserting that the booking was successful

Test client driven rollback:

    11:54:53,001 INFO  [stdout] (http-localhost-127.0.0.1-8080-2) [CLIENT] Beginning the first WS-AT transaction
    11:54:53,081 INFO  [stdout] (http-localhost-127.0.0.1-8080-2) [CLIENT] Calling makeBooking on the WS client stub.
    11:54:53,296 INFO  [stdout] (http-localhost-127.0.0.1-8080-3) [SERVICE] Restaurant service invoked to make a booking for '1'
    11:54:53,297 INFO  [stdout] (http-localhost-127.0.0.1-8080-3) [SERVICE] Using the JPA Entity Manager to update the BookingCountEntity within a JTA transaction
    11:54:53,301 INFO  [stdout] (http-localhost-127.0.0.1-8080-2) [CLIENT] Booking successful, about to rollback the WS-AT transaction
    11:54:54,189 INFO  [stdout] (http-localhost-127.0.0.1-8080-2) [CLIENT] Beginning the second WS-AT transaction
    11:54:54,249 INFO  [stdout] (http-localhost-127.0.0.1-8080-2) [CLIENT] Calling getBookingCount on the WS client stub.
    11:54:54,344 INFO  [stdout] (http-localhost-127.0.0.1-8080-3) [SERVICE] getBookingCount() invoked
    11:54:54,350 INFO  [stdout] (http-localhost-127.0.0.1-8080-2) [CLIENT] Booking count obtained successfully, about to commit the WS-AT transaction.
    11:54:54,690 INFO  [stdout] (http-localhost-127.0.0.1-8080-2) [CLIENT] Asserting that the booking was *not* successful

BridgeFromJTATest
-----------------

Test commit:

    11:54:56,868 INFO  [stdout] (http-localhost-127.0.0.1-8080-1) [CLIENT] Beginning the first JTA transaction
    11:54:56,869 INFO  [stdout] (http-localhost-127.0.0.1-8080-1) [CLIENT] Calling makeBooking on the WS client stub. The registered interceptor will bridge rom JTA to WS-AT
    11:54:57,103 INFO  [stdout] (http-localhost-127.0.0.1-8080-3) [SERVICE] Restaurant service invoked to make a booking for '1'
    11:54:57,104 INFO  [stdout] (http-localhost-127.0.0.1-8080-3) [SERVICE] Using the JPA Entity Manager to update the BookingCountEntity within a JTA transaction
    11:54:57,116 INFO  [stdout] (http-localhost-127.0.0.1-8080-1) [CLIENT] Booking successful, about to commit the JTA transaction. This will also cause the bridged WS-AT transaction to commit
    11:54:57,933 INFO  [stdout] (http-localhost-127.0.0.1-8080-1) [CLIENT] Beginning the second JTA transaction
    11:54:57,934 INFO  [stdout] (http-localhost-127.0.0.1-8080-1) [CLIENT] Calling getBookingCount on the WS client stub. The registered interceptor will bridge rom JTA to WS-AT
    11:54:58,032 INFO  [stdout] (http-localhost-127.0.0.1-8080-3) [SERVICE] getBookingCount() invoked
    11:54:58,037 INFO  [stdout] (http-localhost-127.0.0.1-8080-1) [CLIENT] Booking count obtained successfully, about to commit the JTA transaction. This will also cause the bridged WS-AT transaction to commit
    11:54:58,309 INFO  [stdout] (http-localhost-127.0.0.1-8080-1) [CLIENT] Asserting that the booking was successful

Test client driven rollback:

    11:54:58,770 INFO  [stdout] (http-localhost-127.0.0.1-8080-2) [CLIENT] Beginning the first JTA transaction
    11:54:58,770 INFO  [stdout] (http-localhost-127.0.0.1-8080-2) [CLIENT] Calling makeBooking on the WS client stub. The registered interceptor will bridge rom JTA to WS-AT
    11:54:58,983 INFO  [stdout] (http-localhost-127.0.0.1-8080-3) [SERVICE] Restaurant service invoked to make a booking for '1'
    11:54:58,983 INFO  [stdout] (http-localhost-127.0.0.1-8080-3) [SERVICE] Using the JPA Entity Manager to update the BookingCountEntity within a JTA transaction
    11:54:58,988 INFO  [stdout] (http-localhost-127.0.0.1-8080-2) [CLIENT] Booking successful, about to rollback the JTA transaction. This will also cause the bridged WS-AT transaction to rollback
    11:54:59,327 INFO  [stdout] (http-localhost-127.0.0.1-8080-2) [CLIENT] Beginning the second JTA transaction
    11:54:59,328 INFO  [stdout] (http-localhost-127.0.0.1-8080-2) [CLIENT] Calling getBookingCount on the WS client stub. The registered interceptor will bridge rom JTA to WS-AT
    11:54:59,431 INFO  [stdout] (http-localhost-127.0.0.1-8080-3) [SERVICE] getBookingCount() invoked
    11:54:59,436 INFO  [stdout] (http-localhost-127.0.0.1-8080-2) [CLIENT] Booking count obtained successfully, about to commit the JTA transaction. This will also cause the bridged WS-AT transaction to commit
