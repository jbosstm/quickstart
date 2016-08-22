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

    cp $JBOSS_HOME/docs/examples/configs/standalone-xts.xml $JBOSS_HOME/standalone/configuration/
    mvn clean test -Parq | egrep "stdout|stderr|started in"


Expected Output
---------------

testCommit

    [CLIENT] Beginning the first JTA transaction (to increment the counter)
    [CLIENT] Calling incrementCounter on each client stub. The registered interceptor will bridge rom JTA to WS-AT
    [SERVICE] First service invoked to increment the counter by '1'
    [SERVICE] Using the JPA Entity Manager to update the counter within a JTA transaction
    [SERVICE] Second service invoked to increment the counter by '1'
    [SERVICE] Using the JPA Entity Manager to update the counter within a JTA transaction
    [CLIENT] about to commit the JTA transaction. This will also cause the bridged WS-AT transactions to commit
    [CLIENT] Beginning the second JTA transaction (to check the counter *was* incremented)
    [CLIENT] Calling getCounter on the client stubs. The registered interceptor will bridge rom JTA to WS-AT
    [SERVICE] getCounter() invoked
    [SERVICE] getCounter() invoked
    [CLIENT] Counters obtained successfully, about to commit the JTA transaction. This will also cause the bridged WS-AT transactions to commit
    [CLIENT] Asserting that the counters incremented successfully

testRolback

    [CLIENT] Beginning the first JTA transaction (to increment the counter)
    [CLIENT] Calling incrementCounter on each client stub. The registered interceptor will bridge rom JTA to WS-AT
    [SERVICE] First service invoked to increment the counter by '1'
    [SERVICE] Using the JPA Entity Manager to update the counter within a JTA transaction
    [SERVICE] Second service invoked to increment the counter by '1'
    [SERVICE] Using the JPA Entity Manager to update the counter within a JTA transaction
    [CLIENT] about to rollback the JTA transaction. This will also cause the bridged WS-AT transaction to rollback
    [CLIENT] Beginning the second JTA transaction (to check the counter *was not* incremented)
    [CLIENT] Calling getCounter on the client stubs. The registered interceptor will bridge rom JTA to WS-AT
    [SERVICE] getCounter() invoked
    [SERVICE] getCounter() invoked
    [CLIENT] Counters obtained successfully, about to commit the JTA transaction. This will also cause the bridged WS-AT transaction to commit
    [CLIENT] Asserting that the counters were *not* incremented successfully