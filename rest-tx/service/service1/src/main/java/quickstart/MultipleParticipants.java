package quickstart;

import java.net.HttpURLConnection;

import org.jboss.jbossts.star.util.TxMediaType;
import org.jboss.jbossts.star.util.TxSupport;

public class MultipleParticipants {
    // construct the endpoint for the example web service that will take part in a transaction
    private static final int SERVICE_PORT = 58082;
    private static final String SERVICE_URL =  "http://localhost:" + SERVICE_PORT + '/' + TransactionAwareResource.PSEGMENT;

    public static void main(String[] args) {
        // POSTing to the transaction coordinator url will create a new REST transaction
        String coordinatorUrl="http://localhost:8080/rest-at-coordinator/tx/transaction-manager";

        if (args.length > 0 && args[0].startsWith("coordinator="))
            coordinatorUrl = args[0].substring("coordinator=".length());

        // the example uses an embedded JAX-RS server for running the service that will take part in a transaction
        JaxrsServer.startServer("localhost", SERVICE_PORT);

        // get a helper for using REST Atomic Transactions, passing in the well know resource endpoint for the transaction coordinator
        TxSupport txn = new TxSupport(coordinatorUrl);

        // start a REST Atomic transaction
        txn.startTx();

        /*
         * Send two web service requests. Include the resource url for registering durable participation
         * in the transaction with the request (namely txn.enlistUrl())
         *
         * Each request should cause the service to enlist a unit of work within the transaction.
         */
        String serviceRequest = SERVICE_URL + "?enlistURL=" + txn.getDurableParticipantEnlistmentURI();

        txn.httpRequest(new int[] {HttpURLConnection.HTTP_OK}, serviceRequest, "GET",
                TxMediaType.PLAIN_MEDIA_TYPE, null, null);
        txn.httpRequest(new int[] {HttpURLConnection.HTTP_OK}, serviceRequest, "GET",
                TxMediaType.PLAIN_MEDIA_TYPE, null, null);

        /*
         * Commit the transaction (we expect the service to receive a prepare followed by a commit request for
         * each work unit it enlists)
         * Note that if there was only one work unit then the implementation would skip the prepare step.
         */
        System.out.println("Client: Committing transaction");
        txn.commitTx();

        // the web service should have received prepare and commit requests from the transaction coordinator
        // (TXN_MGR_URL) for each work unit
        String cnt = txn.httpRequest(new int[] {HttpURLConnection.HTTP_OK}, SERVICE_URL + "/query", "GET",
                TxMediaType.PLAIN_MEDIA_TYPE, null, null);

        // shutdown the embedded JAX-RS server
        JaxrsServer.stopServer();

        // check that the service has been asked to commit twice
        if ("2".equals(cnt))
            System.out.println("SUCCESS: Both service work loads received commit requests");
        else
            throw new RuntimeException("FAILURE: At least one server work load did not receive a commit request: " + cnt);
    }
}
