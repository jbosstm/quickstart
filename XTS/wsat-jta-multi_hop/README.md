wsat-jta-multi_hop: JTA -> WS-AT -> JTA -> WS-AT -> JTA
=======================================================
Author: Paul Robinson

Introduction
------------

This quickstart uses JTA and WS-AT. In particular it bridges these two different transaction technologies into a single distributed transaction using the TXBridge. The quickstart is composed of
a client (the test) and two Web services (FirstServiceAT and SecondServiceAT). The Client begins a JTA transaction and then invokes an operation on FirstServiceAT. FirstServiceAT is a Web service
that supports WS-AT. Therefore the TXBridge automatically bridges the JTA transaction to a WS-AT transaction before the invocation is made. FirstServiceAT uses JPA to persist its data.
Therefore, the incoming WS-AT transaction is bridged onto a subordinate JTA transaction for use by JPA. The FirstServiceAT Web Service updates some local data and then invokes the SecondServiceAT
Web services. Similarly, to when invoking FirstServiceAT, the JTA transaction is bridged to a WS-AT transaction when invoking SecondServiceAT. SecondServiceAT also uses JPA for persistence, so
the incoming WS-AT transaction is again bridged to JTA.

Running the Quickstart
----------------------

    cp $JBOSS_HOME/docs/examples/configs/standalone-xts.xml $JBOSS_HOME/standalone/configuration/
    mvn clean test -Parq | egrep "stdout|stderr|started in"


Expected Output
---------------

testCommit

    [CLIENT] Beginning the first JTA transaction XXX
    [CLIENT] Calling incrementCounter on the WS firstClient stub. The registered interceptor will bridge rom JTA to WS-AT
    [SERVICE_1] First service invoked to increment the counter by '1'
    [SERVICE_1] Using the JPA Entity Manager to update the counter within a JTA transaction
    [SERVICE_1] Calling incrementCounter on the WS secondClient stub. The registered interceptor will bridge rom JTA to WS-AT
    [SERVICE_2] Second service invoked to increment the counter by '1'
    [SERVICE_2] Using the JPA Entity Manager to update the counter within a JTA transaction
    [CLIENT] Update successful, about to commit the JTA transaction. This will also cause the bridged WS-AT transaction to commit
    [CLIENT] Beginning the first JTA transaction
    [CLIENT] Calling incrementCounter on the WS firstClient stub. The registered interceptor will bridge rom JTA to WS-AT
    [SERVICE_1] First service invoked to increment the counter by '1'
    [SERVICE_1] Using the JPA Entity Manager to update the counter within a JTA transaction
    [SERVICE_1] Calling incrementCounter on the WS secondClient stub. The registered interceptor will bridge rom JTA to WS-AT
    [SERVICE_2] Second service invoked to increment the counter by '1'
    [SERVICE_2] Using the JPA Entity Manager to update the counter within a JTA transaction
    [CLIENT] Update successful, about to commit the JTA transaction. This will also cause the bridged WS-AT transaction to commit
    [CLIENT] Beginning the second JTA transaction
    [CLIENT] Calling getFirstCounter and getSecondCounter on the WS firstClient stub. The registered interceptor will bridge rom JTA to WS-AT
    [SERVICE_1] getFirstCounter() invoked
    [SERVICE_1] Calling getCounter on the WS secondClient stub. The registered interceptor will bridge rom JTA to WS-AT
    [SERVICE_2] getFirstCounter() invoked
    [CLIENT] Counters obtained successfully, about to commit the JTA transaction. This will also cause the bridged WS-AT transaction to commit
    [CLIENT] Asserting that the counters were incremented successfully

testClientDrivenRollback

    [CLIENT] Beginning the first JTA transaction
    [CLIENT] Calling incrementCounter on the WS firstClient stub. The registered interceptor will bridge rom JTA to WS-AT
    [SERVICE_1] First service invoked to increment the counter by '1'
    [SERVICE_1] Using the JPA Entity Manager to update the counter within a JTA transaction
    [SERVICE_1] Calling incrementCounter on the WS secondClient stub. The registered interceptor will bridge rom JTA to WS-AT
    [SERVICE_2] Second service invoked to increment the counter by '1'
    [SERVICE_2] Using the JPA Entity Manager to update the counter within a JTA transaction
    [CLIENT] Update successful, about to rollback the JTA transaction. This will also cause the bridged WS-AT transaction to rollback
    [CLIENT] Beginning the second JTA transaction
    [CLIENT] Calling getFirstCounter and getSecondCounter on the WS firstClient stub. The registered interceptor will bridge rom JTA to WS-AT
    [CLIENT] Calling getFirstCounter and getSecondCounter on the WS firstClient stub. The registered interceptor will bridge rom JTA to WS-AT
    [SERVICE_1] getFirstCounter() invoked
    [SERVICE_1] Calling getCounter on the WS secondClient stub. The registered interceptor will bridge rom JTA to WS-AT
    [SERVICE_2] getFirstCounter() invoked
    [CLIENT] Counters obtained successfully, about to commit the JTA transaction. This will also cause the bridged WS-AT transaction to commit
    [CLIENT] Asserting that the counter increments were *not* successful

