wsba-participant-completion-simple: Deployment of a WS-BA (WS Business Activity) - Participant Completion, using the TXFramework
================================================================================================================================
Author: Paul Robinson

_NOTE: This quickstart utilises technology that is in status "Tech Preview". Although the quickstart is tested regularly,
you may find issues when developing your own applications using the TXFramework. We are very keen to hear about your
experiences with the TXFramework and of any bugs you may find. Please direct these to the JBossTS forum.

What is it?
-----------

This example demonstrates the deployment of a WS-BA (WS Business Activity) enabled JAX-WS Web service bundled in a war archive for deployment to *JBoss Enterprise Application Platform 6* or *JBoss AS 7*.

The example uses the annotation support provided by the TXFramework. The TXFramwork provides annotation support for
writing transactional applications. This particular example shows how it can be used to write a WS-BA application.
However, the TXFramework provides support for other Transaction protocols, such as REST-AT and WS-AT.
See the other TXFramework quickstarts for more examples and the README.md file in the directory above, for a more complete
description of what the TXFramework provides.

In particular this example showcases the following features of the TXFramework:

1. Annotation support for developing participants. Traditionally a separate participant class needed to be developed that implemented (all methods) of a particular interface. With the TXFramework the developer can simply annotate methods of the application, that participate in the transaction protocol. For example, a method annotated by @Compensate will be invoked at the compensate phase of the protocol.
2. Automatic participant registration. A participant is automatically registered with the transaction. Traditionally, the application needed to do this registration manually.
3. Per-participant data-management. This allows the application to store data in a map that is tied to the transaction participant. When transaction lifecycle methods are invoked, the data for just that transaction is made available through this map.


The quickstart comprises of a simple Web service that accepts orders for items to be purchased and then simulates an email confirmation.
The Service accepts orders within a Business Activity, and is able to compensate the activity by sending a cancellation email.

The example demonstrates the basics of implementing a WS-BA enabled Web service. It is beyond the scope of this quickstart to demonstrate more advanced features. In particular

1. The Service does not implement the required hooks to support recovery in the presence of failures.
2. Only one Web service participates in the protocol. As WS-BA is a coordination protocol, it is best suited to multi-participant scenarios.

For a more complete example, please see the XTS demonstrator application that ships with the JBossTS project: http://www.jboss.org/jbosstm. However, this does not use the TXFramework.

It is also assumed that you have an understanding of WS-BusinessActivity. For more details, read the XTS documentation
that ships with the Narayana project. The latet version can be downloaded from here: http://www.jboss.org/jbosstm/downloads/

The application consists of a single JAX-WS web service that is deployed within a war archive. It is tested with a JBoss
Arquillian enabled JUnit test.

When running the org.jboss.narayana.quickstarts.wsba.participantcompletion.simple.ClientTest#testSuccess() method, the
following steps occur:

1. A new Business Activity is created by the client.
2. An operation on a WS-BA enabled Web service is invoked by the client.
3. The `JaxWSHeaderContextProcessor` in the WS Client handler chain inserts the BA context into the outgoing SOAP message.
4. When the service receives the SOAP request, the `JaxWSHeaderContextProcessor` in its handler chain inspects the BA context and associates the request with this BA.
5. The Web service operation is invoked.
6. The TXFramework automatically enlists a participant in this BA. This allows the Web Service logic to respond to protocol events, such as compensate and close via annotated methods (@Compensate and @Close etc).
7. The service invokes the business logic. In this case, an email send is simulated to confirm the order.
9. Providing the above steps where successful, the service notifies the coordinator that it has completed. The service has now made its changes visible and is not holding any locks. Allowing the service to notify completion is an optimisation that prevents the holding of locks, whilst waiting for other participants to complete. This notification is required as the Service participates in the `ParticipantCompletion` protocol.
10. The client can then decide to complete or cancel the BA. If the client decides to complete, all participants will be told to close. If the participant decides to cancel, all participants will be told to compensate. Which for this example, results in an email being sent confirming cancellation.

There is another test that shows what happens if the client cancels the BA.


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
2. Open a command line and navigate to the root directory of this quickstart.
3. Type the following command to run the test goal with the following profile activated:

        mvn clean test -Parq-jbossas-remote 


Investigate the Console Output
----------------------------

The following expected output should appear. The output explains what actually went on when these tests ran.

Test success:

    08:02:36,791 INFO  [stdout] (http-localhost-127.0.0.1-8080-2) Starting 'testSuccess'. This test invokes a WS within a BA. The BA is later closed, which causes the WS call to complete successfully.
    08:02:36,791 INFO  [stdout] (http-localhost-127.0.0.1-8080-2) [CLIENT] Creating a new Business Activity
    08:02:36,791 INFO  [stdout] (http-localhost-127.0.0.1-8080-2) [CLIENT] Beginning Business Activity (All calls to Web services that support WS-BA wil be included in this activity)
    08:02:36,809 INFO  [stdout] (http-localhost-127.0.0.1-8080-2) [CLIENT] invoking placeOrder('a book') on WS
    08:02:37,038 INFO  [stdout] (http-localhost-127.0.0.1-8080-4) [SERVICE] invoked placeOrder('a book')
    08:02:37,038 INFO  [stdout] (http-localhost-127.0.0.1-8080-4) [SERVICE] Attempt to email an order confirmation, if successful notify the coordinator that we have completed our work
    08:02:37,038 INFO  [stdout] (http-localhost-127.0.0.1-8080-4) [SERVICE] sent email: 'Your order is now confirmed for the following item: 'a book''
    08:02:37,039 INFO  [stdout] (http-localhost-127.0.0.1-8080-4) [SERVICE] Email sent successfully, notifying coordinator of completion
    08:02:37,161 INFO  [stdout] (http-localhost-127.0.0.1-8080-2) [CLIENT] Closing Business Activity (This will cause the BA to complete successfully)

Test cancel:

    08:02:37,517 INFO  [stdout] (http-localhost-127.0.0.1-8080-2) Starting 'testCancel'. This test invokes a WS within a BA. The BA is later cancelled, which causes these WS call to be compensated.
    08:02:37,517 INFO  [stdout] (http-localhost-127.0.0.1-8080-2) [CLIENT] Creating a new Business Activity
    08:02:37,517 INFO  [stdout] (http-localhost-127.0.0.1-8080-2) [CLIENT] Beginning Business Activity (All calls to Web services that support WS-BA will be included in this activity)
    08:02:37,535 INFO  [stdout] (http-localhost-127.0.0.1-8080-2) [CLIENT] invoking placeOrder('a book') on WS
    08:02:37,789 INFO  [stdout] (http-localhost-127.0.0.1-8080-4) [SERVICE] invoked placeOrder('a book')
    08:02:37,789 INFO  [stdout] (http-localhost-127.0.0.1-8080-4) [SERVICE] Attempt to email an order confirmation, if successful notify the coordinator that we have completed our work
    08:02:37,789 INFO  [stdout] (http-localhost-127.0.0.1-8080-4) [SERVICE] sent email: 'Your order is now confirmed for the following item: 'a book''
    08:02:37,789 INFO  [stdout] (http-localhost-127.0.0.1-8080-4) [SERVICE] Email sent successfully, notifying coordinator of completion
    08:02:37,909 INFO  [stdout] (http-localhost-127.0.0.1-8080-2) [CLIENT] Cancelling Business Activity (This will cause the work to be compensated)
    08:02:38,059 INFO  [stdout] (TaskWorker-1) [SERVICE] @Compensate
    08:02:38,060 INFO  [stdout] (TaskWorker-1) [SERVICE] sent email: 'Unfortunately, we have had to cancel your order for item 'a book''
