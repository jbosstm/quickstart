wsat-jta-multi_service: JTA -> 2x(WS-AT -> JTA)
=======================================================
Author: Paul Robinson

Introduction
------------

This quickstart uses JTA and WS-AT. In particular it bridges these two different transaction technologies into a single distributed transaction using the TXBridge. The quickstart is composed of
a client (the test) and two Web services (FirstServiceAT and SecondServiceAT). Both services are invoked by the test from within the same JTA transaction.

The Client begins a JTA transaction and then invokes an operation on each service. FirstServiceAT and SecondServiceAT are Web services that supports WS-AT. Therefore the TXBridge automatically bridges the JTA
transaction to a WS-AT transaction before each invocation is made. Each service uses JPA to persist its data (the value of a counter). Therefore, the incoming WS-AT transaction is also bridged onto
a subordinate JTA transaction for use by JPA.

Running the Quickstart
----------------------

Console 1

    cd $JBOSS_HOME
    cp docs/examples/configs/standalone-xts.xml standalone/configuration/
    sh bin/standalone.sh --server-config=standalone-xts.xml | egrep "stdout|stderr|started in"

Console 2

    mvn clean test -Parq-jbossas-remote


Expected Output
---------------

testCommit

    17:27:15,482 INFO  [stdout] (http-localhost/127.0.0.1:8080-2) [CLIENT] Beginning the first JTA transaction (to increment the counter)
    17:27:15,482 INFO  [stdout] (http-localhost/127.0.0.1:8080-2) [CLIENT] Calling incrementCounter on each client stub. The registered interceptor will bridge rom JTA to WS-AT
    17:27:15,591 INFO  [stdout] (http-localhost/127.0.0.1:8080-4) [SERVICE] First service invoked to increment the counter by '1'
    17:27:15,591 INFO  [stdout] (http-localhost/127.0.0.1:8080-4) [SERVICE] Using the JPA Entity Manager to update the counter within a JTA transaction
    17:27:15,622 INFO  [stdout] (http-localhost/127.0.0.1:8080-4) [SERVICE] Second service invoked to increment the counter by '1'
    17:27:15,622 INFO  [stdout] (http-localhost/127.0.0.1:8080-4) [SERVICE] Using the JPA Entity Manager to update the counter within a JTA transaction
    17:27:15,625 INFO  [stdout] (http-localhost/127.0.0.1:8080-2) [CLIENT] about to commit the JTA transaction. This will also cause the bridged WS-AT transactions to commit
    17:27:15,849 INFO  [stdout] (http-localhost/127.0.0.1:8080-2) [CLIENT] Beginning the second JTA transaction (to check the counter *was* incremented)
    17:27:15,849 INFO  [stdout] (http-localhost/127.0.0.1:8080-2) [CLIENT] Calling getCounter on the client stubs. The registered interceptor will bridge rom JTA to WS-AT
    17:27:15,946 INFO  [stdout] (http-localhost/127.0.0.1:8080-4) [SERVICE] getCounter() invoked
    17:27:15,982 INFO  [stdout] (http-localhost/127.0.0.1:8080-4) [SERVICE] getCounter() invoked
    17:27:15,990 INFO  [stdout] (http-localhost/127.0.0.1:8080-2) [CLIENT] Counters obtained successfully, about to commit the JTA transaction. This will also cause the bridged WS-AT transactions to commit
    17:27:16,215 INFO  [stdout] (http-localhost/127.0.0.1:8080-2) [CLIENT] Asserting that the counters incremented successfully

testRolback

    17:27:16,647 INFO  [stdout] (http-localhost/127.0.0.1:8080-2) [CLIENT] Beginning the first JTA transaction (to increment the counter)
    17:27:16,647 INFO  [stdout] (http-localhost/127.0.0.1:8080-2) [CLIENT] Calling incrementCounter on each client stub. The registered interceptor will bridge rom JTA to WS-AT
    17:27:16,741 INFO  [stdout] (http-localhost/127.0.0.1:8080-4) [SERVICE] First service invoked to increment the counter by '1'
    17:27:16,741 INFO  [stdout] (http-localhost/127.0.0.1:8080-4) [SERVICE] Using the JPA Entity Manager to update the counter within a JTA transaction
    17:27:16,781 INFO  [stdout] (http-localhost/127.0.0.1:8080-4) [SERVICE] Second service invoked to increment the counter by '1'
    17:27:16,781 INFO  [stdout] (http-localhost/127.0.0.1:8080-4) [SERVICE] Using the JPA Entity Manager to update the counter within a JTA transaction
    17:27:16,784 INFO  [stdout] (http-localhost/127.0.0.1:8080-2) [CLIENT] about to rollback the JTA transaction. This will also cause the bridged WS-AT transaction to rollback
    17:27:16,874 INFO  [stdout] (http-localhost/127.0.0.1:8080-2) [CLIENT] Beginning the second JTA transaction (to check the counter *was not* incremented)
    17:27:16,874 INFO  [stdout] (http-localhost/127.0.0.1:8080-2) [CLIENT] Calling getCounter on the client stubs. The registered interceptor will bridge rom JTA to WS-AT
    17:27:16,960 INFO  [stdout] (http-localhost/127.0.0.1:8080-4) [SERVICE] getCounter() invoked
    17:27:16,992 INFO  [stdout] (http-localhost/127.0.0.1:8080-4) [SERVICE] getCounter() invoked
    17:27:16,995 INFO  [stdout] (http-localhost/127.0.0.1:8080-2) [CLIENT] Counters obtained successfully, about to commit the JTA transaction. This will also cause the bridged WS-AT transaction to commit
    17:27:17,236 INFO  [stdout] (http-localhost/127.0.0.1:8080-2) [CLIENT] Asserting that the counters were *not* incremented successfully