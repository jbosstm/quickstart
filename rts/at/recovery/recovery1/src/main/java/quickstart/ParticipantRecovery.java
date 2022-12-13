/*
 * JBoss, Home of Professional Open Source
 * Copyright 2016, Red Hat, Inc., and others contributors as indicated
 * by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */
package quickstart;

import java.net.HttpURLConnection;

import org.jboss.jbossts.star.util.TxMediaType;
import org.jboss.jbossts.star.util.TxSupport;

public class ParticipantRecovery {
    static JAXRSServer txnServer;

    public static void main(String[] args) {
        String coordinatorUrl = null; // the endpoint of the resource for creating transaction coordinators
        String serviceUrl = null; // the endpoint for the example web service that will take part in a transaction
        String opt = "";

        for (String arg : args) {    System.out.printf("checking arg %s%n", arg);
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
        txnServer.addDeployment(new TransactionAwareResource.ServiceApp(), "/");

        // get a helper for using RESTful transactions, passing in the well know resource endpoint for the transaction manager
        TxSupport txn = new TxSupport(coordinatorUrl);

        if ("-r".equals(opt)) {
            System.out.println("=============================================================================");
            System.out.println("Client: WAITING FOR RECOVERY IN 2 SECOND INTERVALS (FOR A MAX OF 130 SECONDS)");
            System.out.println("=============================================================================");

            for (long i = 0L; i < 130L; i += 2) {
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
         * Send two web service requests. Include the resource url for registering durable participation
         * in the transaction with the request
         */
        String serviceRequest = serviceUrl + "?enlistURL=" + txn.getDurableParticipantEnlistmentURI();

        String wId1 = txn.httpRequest(new int[] {HttpURLConnection.HTTP_OK}, serviceRequest,
                "GET", TxMediaType.PLAIN_MEDIA_TYPE, null, null);
        String wId2 = txn.httpRequest(new int[] {HttpURLConnection.HTTP_OK}, serviceRequest,
                "GET", TxMediaType.PLAIN_MEDIA_TYPE, null, null);

        // commit the transaction
        if ("-f".equals(opt)) {
            System.out.printf("Client: Failing work load %s in txn %s%n", wId2, txn.getTxnUri());
            TransactionAwareResource.FAIL_COMMIT = wId2;
        }

        System.out.println("Client: Committing transaction");
        txn.commitTx();

        // the web service should have received prepare and commit requests from the transaction manager

        // shutdown the embedded JAX-RS server
        stopServer();
    }

    public static void startServer(String serviceUrl) {
        int servicePort = Integer.parseInt(serviceUrl.replaceFirst(".*:(.*)/.*", "$1"));
        // the example uses an embedded JAX-RS server for running the service that will take part in a transaction
        txnServer = new JAXRSServer("recovery1", servicePort);
    }

    public static void stopServer() {
        // shutdown the embedded JAX-RS server
        txnServer.stop();
    }
}
