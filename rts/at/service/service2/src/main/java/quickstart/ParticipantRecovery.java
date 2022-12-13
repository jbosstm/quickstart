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

        for (String arg : args) {
            if (arg.startsWith("coordinator="))
                coordinatorUrl = arg.substring("coordinator=".length());
            else if (arg.startsWith("service="))
                serviceUrl = arg.substring("service=".length());
        }

        if (coordinatorUrl == null || serviceUrl == null)
            throw new RuntimeException("Missing coordinator or service URLs");

        startServer(serviceUrl);

        // get a helper for using RESTful transactions, passing in the well known resource endpoint for the transaction manager
        // (however a typical application would use an instance of jakarta.ws.rs.client.Client
        TxSupport txn = new TxSupport(coordinatorUrl);

        // start a REST Atomic transaction
        txn.startTx();

        /*
         * Send two web service requests to ensure that 2PC is used. Include the resource url for registering durable
         * participation in the transaction with the request.
         */
        String participantEnlistmentURL = URLEncoder.encode(txn.getDurableParticipantEnlistmentURI(), StandardCharsets.UTF_8);
        String serviceRequest = String.format("%s?enlistURL=%s", serviceUrl, participantEnlistmentURL);

        txn.httpRequest(new int[] {HttpURLConnection.HTTP_OK}, serviceRequest,
                "GET", TxMediaType.PLAIN_MEDIA_TYPE, null, null);
        txn.httpRequest(new int[] {HttpURLConnection.HTTP_OK}, serviceRequest,
                "GET", TxMediaType.PLAIN_MEDIA_TYPE, null, null);

        // commit the transaction
        System.out.println("Client: Committing transaction");
        txn.commitTx();

        // the web service should have received prepare and commit requests from the transaction manager

        // shutdown the embedded JAX-RS server.
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
