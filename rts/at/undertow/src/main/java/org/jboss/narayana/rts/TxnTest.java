/*
 * Copyright 2015, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
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
package org.jboss.narayana.rts;

import org.jboss.jbossts.star.service.TMApplication;
import org.jboss.jbossts.star.util.TxLinkNames;

import org.jboss.logging.Logger;

import com.arjuna.ats.arjuna.common.arjPropertyManager;
import com.arjuna.ats.arjuna.common.recoveryPropertyManager;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Link;
import jakarta.ws.rs.core.MediaType;

import java.io.IOException;

import java.util.Objects;
import java.util.Set;

public class TxnTest {
    protected final static Logger log = Logger.getLogger(TxnTest.class);
    static final int TXN_PORT = 8090;
    static final int SVC1_PORT = 8092;
    static final int SVC2_PORT = 8094;

    static final String TXN_URL = String.format("http://%s:%d%s", "localhost", TXN_PORT, "/tx/transaction-manager");
    static final String SVC1_URL = String.format("http://%s:%d%s", "localhost", SVC1_PORT, "/eg/service");
    static final String SVC2_URL = String.format("http://%s:%d%s", "localhost", SVC2_PORT, "/eg/service");

    static JAXRSServer txnServer;
    static JAXRSServer svc1Server;
    static JAXRSServer svc2Server;

    static Client txnClient;
    static Client svc1Client;
    static Client svc2Client;

    public static void setup(){
        arjPropertyManager.getCoordinatorEnvironmentBean().setTransactionStatusManagerEnable(false);
        recoveryPropertyManager.getRecoveryEnvironmentBean().setRecoveryListener(false);
        arjPropertyManager.getCoordinatorEnvironmentBean().setTxReaperZombieMax(0);
        
        
        txnServer = new JAXRSServer("coordinator", TXN_PORT);
        txnServer.addDeployment(new TMApplication(), "/");

        svc1Server = new JAXRSServer("service 1", SVC1_PORT);
        svc1Server.addDeployment(new TransactionAwareResource.ServiceApp(), "eg");

        svc2Server = new JAXRSServer("service 2", SVC2_PORT);
        svc2Server.addDeployment(new TransactionAwareResource.ServiceApp(), "eg");

        // These are used in the example to programmatically execute the transaction
        txnClient = ClientBuilder.newClient();
        svc1Client = ClientBuilder.newClient();
        svc2Client = ClientBuilder.newClient();
    }

    public static void tearDown() {
        txnClient.close();
        svc1Client.close();
        svc2Client.close();
        txnServer.stop();

        svc1Server.stop();
        svc2Server.stop();
    }

    public static int runTxn() {
        try {

            log.tracef("[%s] BEGINING%n", Thread.currentThread().getName());
            Set<Link> links = TxnHelper.beginTxn(txnClient, TXN_URL);
            log.tracef("[%s] BEGUN%n", Thread.currentThread().getName());
            Link enlistmentLink = TxnHelper.getLink(links, TxLinkNames.PARTICIPANT);

            String serviceRequest1 = String.format("%s?enlistURL=%s", SVC1_URL, Objects.requireNonNull(enlistmentLink).getUri());
            svc1Client.target(serviceRequest1).request().post(Entity.entity("value", MediaType.TEXT_PLAIN_TYPE));
            String serviceRequest2 = String.format("%s?enlistURL=%s", SVC2_URL, enlistmentLink.getUri());
            svc2Client.target(serviceRequest2).request().post(Entity.entity("value", MediaType.TEXT_PLAIN_TYPE));

            log.tracef("[%s] ENDING%n", Thread.currentThread().getName());
            int sc = TxnHelper.endTxn(txnClient, links);
            log.tracef("[%s] ENDED%n", Thread.currentThread().getName());

            String svc1Value = ClientBuilder.newClient().target(SVC1_URL + "/query").request().buildGet().invoke(String.class);
            System.out.printf("Service 1 value:%s%n", svc1Value);

            String svc2Value = ClientBuilder.newClient().target(SVC2_URL + "/query").request().buildGet().invoke(String.class);
            System.out.printf("Service 2 value:%s%n", svc2Value);

            return sc;
        } catch (Throwable e) {
            System.out.printf("runTxn: %s%n", e);
            e.printStackTrace();
            return -1;
        }
    }

    public static void main(String[] args) throws Exception {
        log.tracef("setup%n");
        setup();

        if (args.length > 0 && "-i".equals(args[0]))
            System.in.read(new byte[100]);

        runTxn();

        log.tracef("tearDown%n");
        tearDown();
    }
}
