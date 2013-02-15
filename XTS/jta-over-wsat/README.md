JTA-over-WSAT: A simple example of using WS-AT to create a distributed JTA Transaction
======================================================================================
Author: Paul Robinson


What is it?
-----------
This example demonstrates a JTA client that invokes a remote EJB over Web services. The JTA transaction is distributed to the remote EJB using WS-AtomicTransaction.

Use-case
--------
A distributed ACID transaction is required, but for one of the following reasons JTS is not appropriate:

1) The transaction is distributed over multiple vendors' application servers and their JTS implementations don't inter-operate.
2) IIOP can't be used as the transport, maybe due to firewall issues, so HTTP is required.
3) One or more participants does not support JTS, but they do all support WS-AT.
4) Some other value-add is required that can be provided by Web services, but not Corba.
5) Some other reason, not listed here.

WS-AT is selected as an alternative to JTS.

This Example
------------

The Web service is offered by a Restaurant for making bookings. When it is invoked by the client it simply increments a counter by the number of seats in the booking.
The counter is stored in a database and updated via JPA. The distributed transaction, begun by the client, is used to make this update. Therefore, if the client decides
to rollback the transaction, the update to the counter is also rolled back.


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

The following expected output should appear. Note there will be some other log messages interlaced between these. The output explains what actually went on when these tests ran.

SimpleJTATests
---------------

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
