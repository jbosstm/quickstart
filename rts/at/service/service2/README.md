OVERVIEW
--------

This example builds on the service2 quickstart by showing how you can make your web services transactional
and still recover when the VM hosting the service crashes whilst the transaction is being committed.
[In this version NettyJaxrsServer (instead of UndertowJaxrsServer) is used to deploy the services, mainly
because it simplifies deploying the REST-AT integration API (org.jboss.narayana.rest.integration.ParticipantResource].

The differences are that participants:
- must either implement java.io.Serializable or org.jboss.narayana.rest.integration.api.PersistableParticipant
- must register a deserializer for participants (org.jboss.narayana.rest.integration.api.ParticipantsManager#registerDeserializer (see JaxrsServer.java in the src folder).

USAGE
-----
Prior to running the example make sure that the [REST-AT coordinator is deployed](../../README.md#usage).
If you run the REST-AT coordinator on an endpoint url different from the default you will need to
update the java source file recovery1/src/main/java/quickstart/ParticipantRecovery.java and replace
coordinatorUrl with the correct url.

To test recovery you will need to run the example twice, once to generate a failure condition:

    mvn -P fail clean compile exec:java

or

    mvn exec:java -Dexec.mainClass=quickstart.ParticipantRecovery -Dexec.args="coordinator=http://localhost:8080/rest-at-coordinator/tx/transaction-manager service=http://localhost:58082/service -f"

and a second time in order to test that the web service is asked to replay the commit phase:

    mvn -P recover exec:java

or

    mvn exec:java -Dexec.mainClass=quickstart.ParticipantRecovery -Dexec.args="coordinator=http://localhost:8080/rest-at-coordinator/tx/transaction-manager service=http://localhost:58082/service -r"

Use the run.sh or run.bat script to run both parts together.

If you just want the same behaviour as the service example - ie without failures then run the example without
any arguments:

    mvn clean compile exec:java -Dexec.mainClass=quickstart.ParticipantRecovery -Dexec.args="coordinator=http://localhost:8080/rest-at-coordinator/tx/transaction-manager service=http://localhost:58082/service"


EXPECTED OUTPUT
---------------

[The examples run under the control of maven so you will need to filter maven output from example output.]

You will see lines of output showing the the two JAX-RS services being registered:

    INFO: Root resource classes found:
      class org.jboss.narayana.rest.integration.ParticipantResource
      class quickstart.TransactionAwareResource

On the first run a message showing the client committing transaction:

    Client: Failing work load 2
    Client: Committing transaction

You will then see 5 messages from the participants showing requests from the transaction coordinator
asking participants to prepare and commit their work. The final message is from the second participant 
saying that it is going to halt the VM:

    Service: preparing: wId=2
    Service: preparing: wId=1
    Service: committing: wId=1
    Service: committing: wId=2
    Service: Halting VM during commit of work unit wId=2

On the second run when recovering after generating the failure you should see the following messages:

    =============================================================================
    Client: WAITING FOR RECOVERY IN 2 SECOND INTERVALS (FOR A MAX OF 130 SECONDS)
    =============================================================================
    Service: PUT request to terminate url: wId=2, status:=txStatus=TransactionCommitted
    SUCCESS participant was recovered after 22 seconds. Number of commits: 1

The first message states that the example is waiting for the recovery system (running in the AS
you deployed the war to) to perform a recovery scan. It is highlighted so you know that the test
isn't hanging. The default interval between recovery scans is 120 seconds.

The second output line shows that the web service received a prepare request and hence recovery ran
successfully. And the final output line indicates a successful run.


WHAT JUST HAPPENED?
-------------------

1. We deployed a JAX-RS servlet that implements a RESTful interface to the Narayana transaction manager (TM)
(running in an AS7 or AS6 container).

2. The client (MultipleParticpants.java) started an embedded web server (JaxrsServer.java) for hosting web services.

3. The client then started a REST Atomic Transaction and got back two urls: one for completing the transaction
and one for use with enlisting durable participants (implemented by the class TransactionAwareResource.java)
into the transaction.

4. The client then made two HTTP requests to a web service and passed the participant enlistment url as part
of the context of the request (using a query parameter).

5. The participants used the enlistment url to join the transaction. In this naive example we assumes that
each request is for a separate unit of transactional work and in this way we end up with multiple participants
being involved in the transaction. Having more than one participant means we can demonstrate that either all
participants will commit or none them will.

6. The client sets a static variable on the second participant to force it to halt the VM just before
it commits its work. This will only work with an embedded web server running in the same VM as the client.
If you wanted to change the example so that the service runs in a separate VM then you might instead send the
the service a request telling it to halt during prepare.

7. The client commits the transaction using the resource url it got back when it created the transaction.

8. The transaction manager implementation knows that there are two resources involved and asks them both to
prepare their work (using the resource urls it got from the participants when they enlisted into the transaction).
The integration API receives these requests and calls the service particpant prepare callback.
If the participant resources respond with the correct Vote the transaction manager
proceeds to request commitment. Refer to the REST Atomic Transactions spec for full details.

9. On the first run the second participant halts the VM leaving a recovery record in the AS in need of recover.

10. On the second run the example waits for the service to indicate when the recovery system has called commit
in order to complete the transaction.
