wsba-jta: Using a Distributed Compensation-Based Transaction to Update a DB within a JTA Transaction
====================================================================================
Author: Paul Robinson

What is it?
-----------

This example shows you how to use a distributed compensation based transaction to make updates to a database in a JTA transaction.

Use-case
--------

An application wishes to make its service available to clients outside of its business domain. Its clients also have a requirement
to create a composite service out of this service and others. The client requires transactional guarantees that ensure a consistent
outcome is always achieved even in the case of a service or network failure.

A distributed ACID transaction would provide these guarantees and, from the Client's point of view, it would be a favourable
solution. However, the protocol used in an ACID transaction (2PC) is inherently a blocking protocol. As a result any participant
(in this case the service) would need to hold locks on internal data until told to release them by the coordinator. For many
applications this is not acceptable as the coordinator may not be trusted or the transaction may simply run for too long leaving
locks held for long periods of time.

Instead the service uses a compensation based transaction. Here the service releases locks when it has completed its work.
This can be well in advance of the completion of the transaction. The transactional guarantees ensure that the application
will later be told the outcome of the transaction, giving it the opportunity to compensate any work should the transaction
have failed. What's more, the service is guaranteed to find out the outcome (for any completed work) even if one or more
parties fail.


This Example
------------

This example uses compensating transactions provided by WS Business Activity (WS-BA). A single service is used for simplicity, but
multiple services could also be used.

The quickstart comprises of a simple Web service that accepts purchase orders and then uses JPA to store the order in a database. The database update is done within a JTA transaction
that is committed immediately. The Business activity will either be compensated or confirmed (closed in WS-BA parlance).
The application updates the database to mark the PO as confimed if the BA closes, and marks it as cancelled if the BA is compensated. These database updates are done in a
separate JTA Transaction to the one that created the PO. This is important as Business Activities are designed to support longer running interactions that may cross multiple domains. Therefore it is not advisable
to keep a JTA transaction open for this duration.

It is also assumed that you have an understanding of WS-BA. For more details, read the XTS documentation
that ships with the Narayana project. The latest version can be downloaded from here: http://www.jboss.org/jbosstm/downloads/

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

    16:16:31,583 INFO  [stdout] (http-localhost/127.0.0.1:8080-5) Starting 'testSuccess'. This test invokes a WS within a BA. The BA is later closed, which causes the WS call to complete successfully.
    16:16:31,583 INFO  [stdout] (http-localhost/127.0.0.1:8080-5) [CLIENT] Creating a new Business Activity
    16:16:31,583 INFO  [stdout] (http-localhost/127.0.0.1:8080-5) [CLIENT] Beginning Business Activity (All calls to Web services that support WS-BA wil be included in this activity)
    16:16:31,794 INFO  [stdout] (http-localhost/127.0.0.1:8080-5) [CLIENT] invoking placeOrder('a book') on WS
    16:16:31,979 INFO  [stdout] (http-localhost/127.0.0.1:8080-20) [SERVICE] invoked placeOrder('a book')
    16:16:32,405 INFO  [stdout] (http-localhost/127.0.0.1:8080-5) [CLIENT] Closing Business Activity (This will cause the BA to complete successfully)
    16:16:32,721 INFO  [stdout] (TaskWorker-2) [SERVICE] @Confirm (The participant should confirm any work done within this BA)

Test cancel:

    16:16:32,952 INFO  [stdout] (http-localhost/127.0.0.1:8080-62) Starting 'testCancel'. This test invokes a WS within a BA. The BA is later cancelled, which causes these WS call to be compensated.
    16:16:32,952 INFO  [stdout] (http-localhost/127.0.0.1:8080-62) [CLIENT] Creating a new Business Activity
    16:16:32,952 INFO  [stdout] (http-localhost/127.0.0.1:8080-62) [CLIENT] Beginning Business Activity (All calls to Web services that support WS-BA will be included in this activity)
    16:16:32,998 INFO  [stdout] (http-localhost/127.0.0.1:8080-62) [CLIENT] invoking placeOrder('a book') on WS
    16:16:33,142 INFO  [stdout] (http-localhost/127.0.0.1:8080-71) [SERVICE] invoked placeOrder('a book')
    16:16:33,274 INFO  [stdout] (http-localhost/127.0.0.1:8080-62) [CLIENT] Cancelling Business Activity (This will cause the work to be compensated)
    16:16:33,440 INFO  [stdout] (TaskWorker-1) [SERVICE] @Compensate (The participant should compensate any work done within this BA)
