package quickstart;

import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.jboss.jbossts.star.util.TxMediaType;
import org.jboss.jbossts.star.util.TxSupport;

// see the quickstart README for details of the quickstart
public class ParticipantRecovery {
    static JAXRSServer txnServer;

    public static void main(String[] args) {
        String coordinatorUrl = null; // the endpoint of the resource for creating transaction coordinators
        String serviceUrl = null; // the endpoint for the local example JAX-RS service that will take part in a transaction
        String opt = "";

        for (String arg : args) {
            if (arg.startsWith("coordinator="))
                coordinatorUrl = arg.substring("coordinator=".length());
            else if (arg.startsWith("service="))
                serviceUrl = arg.substring("service=".length());
            else if (arg.startsWith("-"))
                opt = arg;
        }

        if (coordinatorUrl == null || serviceUrl == null)
            throw new RuntimeException("Missing coordinator or service URLs");

        startServer(serviceUrl);

        // get a helper for using RESTful transactions, passing in the well known resource endpoint for the transaction manager
        // (however a typical application would use an instance of jakarta.ws.rs.client.Client
        TxSupport txn = new TxSupport(coordinatorUrl);

        if ("-r".equals(opt)) {
            // wait for recovery of a crashed REST-AT transaction to recover
            System.out.println("=============================================================================");
            System.out.println("Client: WAITING FOR RECOVERY IN 2 SECOND INTERVALS (FOR A MAX OF 130 SECONDS)");
            System.out.println("=============================================================================");

            for (long i = 0l; i < 130l; i += 2) {
                try {
                    // ask the service how many transactions it has committed since the VM started

                    String commitCnt = txn.httpRequest(new int[] {HttpURLConnection.HTTP_OK},
                            serviceUrl + "/commits", "GET", TxMediaType.PLAIN_MEDIA_TYPE, null, null);
                    String abortCnt = txn.httpRequest(new int[] {HttpURLConnection.HTTP_OK},
                            serviceUrl + "/aborts", "GET", TxMediaType.PLAIN_MEDIA_TYPE, null, null);

                    if (commitCnt != null && !"0".equals(commitCnt)) {
                        System.out.println("SUCCESS participant was recovered after " + (i * 2) + " seconds. Number of commits: " + commitCnt);
                        System.exit(0);
                    } else if (abortCnt != null && !"0".equals(abortCnt)) {
                        System.out.println("Partial SUCCESS participant was aborted after " + (i * 2) + " seconds. Number of aborts: " + abortCnt);
                        System.exit(0);
                    }

                    System.out.print(".");
                    Thread.sleep(2000);

                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            System.out.println("FAILURE participant was not recovered");
            System.exit(1);
        }

        // start a REST Atomic transaction
        txn.startTx();

        /*
         * Send two web service requests to ensure that 2PC is used. Include the resource url for registering durable
         * participation in the transaction with the request.
         */
        String participantEnlistmentURL = URLEncoder.encode(txn.getDurableParticipantEnlistmentURI(), StandardCharsets.UTF_8);
        String serviceRequest = String.format("%s?enlistURL=%s", serviceUrl, participantEnlistmentURL);

        String wId1 = txn.httpRequest(new int[] {HttpURLConnection.HTTP_OK}, serviceRequest,
                "GET", TxMediaType.PLAIN_MEDIA_TYPE, null, null);
        String wId2 = txn.httpRequest(new int[] {HttpURLConnection.HTTP_OK}, serviceRequest,
                "GET", TxMediaType.PLAIN_MEDIA_TYPE, null, null);

        // commit the transaction
        if ("-f".equals(opt)) {
            System.out.printf("Client: Failing work load %s in txn %s%n", wId2, txn.getTxnUri());
            TransactionAwareResource.FAIL_COMMIT = wId2; // when participant wId2 commits it will halt the JVM
        }

        System.out.println("Client: Committing transaction");
        txn.commitTx();

        // the web service should have received prepare and commit requests from the transaction manager

        // shutdown the embedded JAX-RS server.
        // nb. not reached if ("-f".equals(opt))
        stopServer();
    }

    public static void startServer(String serviceUrl) {
        int servicePort = Integer.parseInt(serviceUrl.replaceFirst(".*:(.*)/.*", "$1"));
        // the example uses an embedded JAX-RS server for running the service that will take part in a transaction
        txnServer = new JAXRSServer("localhost", servicePort);
    }

    public static void stopServer() {
        // shutdown the embedded JAX-RS server
        txnServer.stop();
    }
}