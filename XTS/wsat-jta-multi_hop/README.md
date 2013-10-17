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

Console 1

    cd $JBOSS_HOME
    cp docs/examples/configs/standalone-xts.xml standalone/configuration/
    sh bin/standalone.sh --server-config=standalone-xts.xml

Console 2

    mvn clean test -Parq-jbossas-remote | egrep "stdout|stderr|started in"


Expected Output
---------------

testCommit

    12:36:42,752 INFO  [stdout] (http-/127.0.0.1:8080-2) [CLIENT] Beginning the first JTA transaction
    12:36:42,757 INFO  [stdout] (http-/127.0.0.1:8080-2) [CLIENT] Calling incrementCounter on the WS firstClient stub. The registered interceptor will bridge rom JTA to WS-AT
    12:36:43,244 INFO  [stdout] (http-/127.0.0.1:8080-3) [SERVICE_1] First service invoked to increment the counter by '1'
    12:36:43,244 INFO  [stdout] (http-/127.0.0.1:8080-3) [SERVICE_1] Using the JPA Entity Manager to update the counter within a JTA transaction
    12:36:43,425 INFO  [stdout] (http-/127.0.0.1:8080-3) [SERVICE_1] Calling incrementCounter on the WS secondClient stub. The registered interceptor will bridge rom JTA to WS-AT
    12:36:43,641 INFO  [stdout] (http-/127.0.0.1:8080-4) [SERVICE_2] Second service invoked to increment the counter by '1'
    12:36:43,641 INFO  [stdout] (http-/127.0.0.1:8080-4) [SERVICE_2] Using the JPA Entity Manager to update the counter within a JTA transaction
    12:36:43,670 INFO  [stdout] (http-/127.0.0.1:8080-2) [CLIENT] Update successful, about to commit the JTA transaction. This will also cause the bridged WS-AT transaction to commit
    12:36:44,517 INFO  [stdout] (http-/127.0.0.1:8080-2) [CLIENT] Beginning the second JTA transaction
    12:36:44,517 INFO  [stdout] (http-/127.0.0.1:8080-2) [CLIENT] Calling getFirstCounter and getSecondCounter on the WS firstClient stub. The registered interceptor will bridge rom JTA to WS-AT
    12:36:44,645 INFO  [stdout] (http-/127.0.0.1:8080-3) [SERVICE_1] getFirstCounter() invoked
    12:36:44,715 INFO  [stdout] (http-/127.0.0.1:8080-3) [SERVICE_1] Calling getCounter on the WS secondClient stub. The registered interceptor will bridge rom JTA to WS-AT
    12:36:44,834 INFO  [stdout] (http-/127.0.0.1:8080-4) [SERVICE_2] getFirstCounter() invoked
    12:36:44,843 INFO  [stdout] (http-/127.0.0.1:8080-2) [CLIENT] Counters obtained successfully, about to commit the JTA transaction. This will also cause the bridged WS-AT transaction to commit
    12:36:45,398 INFO  [stdout] (http-/127.0.0.1:8080-2) [CLIENT] Asserting that the counters were incremented successfully

testClientDrivenRollback

    12:36:46,189 INFO  [stdout] (http-/127.0.0.1:8080-2) [CLIENT] Beginning the first JTA transaction
    12:36:46,189 INFO  [stdout] (http-/127.0.0.1:8080-2) [CLIENT] Calling incrementCounter on the WS firstClient stub. The registered interceptor will bridge rom JTA to WS-AT
    12:36:46,300 INFO  [stdout] (http-/127.0.0.1:8080-3) [SERVICE_1] First service invoked to increment the counter by '1'
    12:36:46,300 INFO  [stdout] (http-/127.0.0.1:8080-3) [SERVICE_1] Using the JPA Entity Manager to update the counter within a JTA transaction
    12:36:46,303 INFO  [stdout] (http-/127.0.0.1:8080-3) [SERVICE_1] Calling incrementCounter on the WS secondClient stub. The registered interceptor will bridge rom JTA to WS-AT
    12:36:46,414 INFO  [stdout] (http-/127.0.0.1:8080-4) [SERVICE_2] Second service invoked to increment the counter by '1'
    12:36:46,414 INFO  [stdout] (http-/127.0.0.1:8080-4) [SERVICE_2] Using the JPA Entity Manager to update the counter within a JTA transaction
    12:36:46,420 INFO  [stdout] (http-/127.0.0.1:8080-2) [CLIENT] Update successful, about to rollback the JTA transaction. This will also cause the bridged WS-AT transaction to rollback
    12:36:46,650 INFO  [stdout] (http-/127.0.0.1:8080-2) [CLIENT] Beginning the second JTA transaction
    12:36:46,650 INFO  [stdout] (http-/127.0.0.1:8080-2) [CLIENT] Calling getFirstCounter and getSecondCounter on the WS firstClient stub. The registered interceptor will bridge rom JTA to WS-AT
    12:36:46,650 INFO  [stdout] (http-/127.0.0.1:8080-2) [CLIENT] Calling getFirstCounter and getSecondCounter on the WS firstClient stub. The registered interceptor will bridge rom JTA to WS-AT
    12:36:46,763 INFO  [stdout] (http-/127.0.0.1:8080-3) [SERVICE_1] getFirstCounter() invoked
    12:36:46,800 INFO  [stdout] (http-/127.0.0.1:8080-3) [SERVICE_1] Calling getCounter on the WS secondClient stub. The registered interceptor will bridge rom JTA to WS-AT
    12:36:46,928 INFO  [stdout] (http-/127.0.0.1:8080-4) [SERVICE_2] getFirstCounter() invoked
    12:36:46,939 INFO  [stdout] (http-/127.0.0.1:8080-2) [CLIENT] Counters obtained successfully, about to commit the JTA transaction. This will also cause the bridged WS-AT transaction to commit
    12:36:47,470 INFO  [stdout] (http-/127.0.0.1:8080-2) [CLIENT] Asserting that the counter increments were *not* successful

