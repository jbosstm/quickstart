wsba-participant-completion-simple: A simple example of using compensation-based transactions with WS-BA
========================================================================================================
Author: Paul Robinson

What is it?
-----------

The example shows how transactional guarantees can be provided in situations where a rollback is not applicable. A Compensation-based transactions
is provided via WS-BusinessActivity.

Use-case
--------
Transactional guarantees are required (atomic outcome despite failures), but one or more of the resources do not support rollback.
For example, sending of an email can not be part of a transaction as you can't rollback the send after it has completed.

The quickstart
--------------
The quickstart comprises of a simple Web service that accepts orders for items to be purchased and then simulates an email confirmation.
The Service accepts orders within a Business Activity, and is able to compensate the activity by sending a cancellation email.

The example demonstrates the basics of implementing a WS-BA enabled Web service. It is beyond the scope of this quickstart to demonstrate more advanced features. In particular

1. The Service does not implement the required hooks to support recovery in the presence of failures.
2. Only one Web service participates in the protocol. As WS-BA is a coordination protocol, it is best suited to multi-participant scenarios.

It is also assumed that you have an understanding of WS-BusinessActivity. For more details, read the XTS documentation
that ships with the Narayana project. The latet version can be downloaded from here: http://www.jboss.org/jbosstm/downloads/

The application consists of a single JAX-WS web service that is deployed within a war archive. It is tested with a JBoss
Arquillian enabled JUnit test.


System requirements
-------------------

All you need to build this project is Java 6.0 (Java SDK 1.6) or better, Maven 3.0 or better.


Start the application server with the Custom Options
----------------------

You need to start the application server, with the XTS sub system enabled. This is enabled through the optional server configuration *standalone-xts.xml*. To do this, run the following commands from the top-level directory of the application server:

        For Linux:     ./bin/standalone.sh --server-config=../../docs/examples/configs/standalone-xts.xml | egrep "stdout|stderr|started"
        For Windows:   \bin\standalone.bat --server-config=..\..\docs\examples\configs\standalone-xts.xml

Note the pipe to `egrep` on the linux command. this filters the output from the server and makes it easier to view the expected output. This is useful for running this quickstart, but would
hide useful output if it was always used during development or production.

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

    13:20:08,190 INFO  [stdout] (http-localhost/127.0.0.1:8080-1) Starting 'testSuccess'. This test invokes a WS within a BA. The BA is later closed, which causes the WS call to complete successfully.
    13:20:08,190 INFO  [stdout] (http-localhost/127.0.0.1:8080-1) [CLIENT] Creating a new Business Activity
    13:20:08,190 INFO  [stdout] (http-localhost/127.0.0.1:8080-1) [CLIENT] Beginning Business Activity (All calls to Web services that support WS-BA wil be included in this activity)
    13:20:08,400 INFO  [stdout] (http-localhost/127.0.0.1:8080-1) [CLIENT] invoking placeOrder('test@test.com, a book') on WS
    13:20:08,582 INFO  [stdout] (http-localhost/127.0.0.1:8080-3) [SERVICE] invoked placeOrder('a book')
    13:20:08,582 INFO  [stdout] (http-localhost/127.0.0.1:8080-3) [SERVICE] Attempt to email an order confirmation. Failure would raise an exception causing the coordinator to be informed that this participant cannot complete.
    13:20:08,582 INFO  [stdout] (http-localhost/127.0.0.1:8080-3) [SERVICE] sent email: 'Order confirmed' to: 'test@test.com'
    13:20:08,680 INFO  [stdout] (http-localhost/127.0.0.1:8080-1) [CLIENT] Closing Business Activity (This will cause the BA to complete successfully)

Test cancel:

    13:20:09,064 INFO  [stdout] (http-localhost/127.0.0.1:8080-1) Starting 'testCancel'. This test invokes a WS within a BA. The BA is later cancelled, which causes these WS call to be compensated.
    13:20:09,064 INFO  [stdout] (http-localhost/127.0.0.1:8080-1) [CLIENT] Creating a new Business Activity
    13:20:09,064 INFO  [stdout] (http-localhost/127.0.0.1:8080-1) [CLIENT] Beginning Business Activity (All calls to Web services that support WS-BA will be included in this activity)
    13:20:09,084 INFO  [stdout] (http-localhost/127.0.0.1:8080-1) [CLIENT] invoking placeOrder('test@test.com, a book') on WS
    13:20:09,159 INFO  [stdout] (http-localhost/127.0.0.1:8080-3) [SERVICE] invoked placeOrder('a book')
    13:20:09,159 INFO  [stdout] (http-localhost/127.0.0.1:8080-3) [SERVICE] Attempt to email an order confirmation. Failure would raise an exception causing the coordinator to be informed that this participant cannot complete.
    13:20:09,160 INFO  [stdout] (http-localhost/127.0.0.1:8080-3) [SERVICE] sent email: 'Order confirmed' to: 'test@test.com'
    13:20:09,210 INFO  [stdout] (http-localhost/127.0.0.1:8080-1) [CLIENT] Cancelling Business Activity (This will cause the work to be compensated)
    13:20:09,352 INFO  [stdout] (TaskWorker-1) [SERVICE] @Compensate called
    13:20:09,352 INFO  [stdout] (TaskWorker-1) [SERVICE] sent email: 'Order cancelled' to: 'test@test.com'

Test application exception:

    13:20:09,477 INFO  [stdout] (http-localhost/127.0.0.1:8080-1) Starting 'testApplicationException'. This test invokes a WS within a BA. The order is made with an invalid email address which causes the placeOrder operation to fail. As a res
    ult the service throws an exception and the coordinator is informed that the BA cannot complete.
    13:20:09,477 INFO  [stdout] (http-localhost/127.0.0.1:8080-1) [CLIENT] Creating a new Business Activity
    13:20:09,477 INFO  [stdout] (http-localhost/127.0.0.1:8080-1) [CLIENT] Beginning Business Activity (All calls to Web services that support WS-BA will be included in this activity)
    13:20:09,495 INFO  [stdout] (http-localhost/127.0.0.1:8080-1) [CLIENT] invoking placeOrder('test@test, a book') on WS
    13:20:09,576 INFO  [stdout] (http-localhost/127.0.0.1:8080-3) [SERVICE] invoked placeOrder('a book')
    13:20:09,577 INFO  [stdout] (http-localhost/127.0.0.1:8080-3) [SERVICE] Attempt to email an order confirmation. Failure would raise an exception causing the coordinator to be informed that this participant cannot complete.
    13:20:09,577 INFO  [stdout] (http-localhost/127.0.0.1:8080-3) [SERVICE] Unable to send email due to an invalid address: 'test@test'. We currently only support '.com' addresses
    13:20:09,700 INFO  [stdout] (http-localhost/127.0.0.1:8080-1) [CLIENT] 'placeOrder' failed, so canceling the BA
