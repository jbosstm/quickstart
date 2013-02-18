/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
 * See the copyright.txt in the distribution for a full listing
 * of individual contributors.
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
 *
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
package org.jboss.narayana.quickstarts.wsba.wsbaToJta;

import com.arjuna.mw.wst11.UserBusinessActivity;
import com.arjuna.mw.wst11.UserBusinessActivityFactory;
import com.arjuna.mw.wst11.client.JaxWSHeaderContextProcessor;
import com.arjuna.wst.TransactionRolledBackException;
import junit.framework.Assert;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.narayana.quickstarts.wsba.wsbaToJta.jaxws.OrderServiceBA;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import javax.xml.ws.handler.Handler;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@RunWith(Arquillian.class)
public class ClientTest {

    private static final String ManifestMF = "Manifest-Version: 1.0\n"
            + "Dependencies: org.jboss.narayana.txframework,org.jboss.xts,org.jboss.jts\n";

    @Deployment
    public static JavaArchive createTestArchive() {

        return ShrinkWrap.create(JavaArchive.class, "test2.jar")
                .addPackages(true, OrderServiceBAImpl.class.getPackage().getName())
                .addAsManifestResource("persistence.xml")
                .addAsManifestResource(EmptyAsset.INSTANCE, ArchivePaths.create("beans.xml"))
                .setManifest(new StringAsset(ManifestMF));
    }

    /**
     * Test the simple scenario where an order is made within a Business Activity which is closed successfully.
     *
     * @throws Exception if something goes wrong.
     */
    @Test
    public void testSuccess() throws Exception {

        System.out
                .println("\n\nStarting 'testSuccess'. This test invokes a WS within a BA. The BA is later closed, which causes the WS call to complete successfully.");
        System.out.println("[CLIENT] Creating a new Business Activity");
        UserBusinessActivity uba = UserBusinessActivityFactory.userBusinessActivity();
        try {
            String value = "a book";

            System.out
                    .println("[CLIENT] Beginning Business Activity (All calls to Web services that support WS-BA wil be included in this activity)");
            uba.begin();

            System.out.println("[CLIENT] invoking placeOrder('" + value + "') on WS");
            OrderServiceBA client = createClient();
            Integer orderId = client.placeOrder(value);

            Assert.assertTrue("Expected order to be in status pending, but it wasn't", client.getOrderStatus(orderId).equals(OrderStatus.PENDING));

            System.out.println("[CLIENT] Closing Business Activity (This will cause the BA to complete successfully)");
            uba.close();

            Assert.assertTrue("Expected order to be confirmed, but it wasn't", client.getOrderStatus(orderId).equals(OrderStatus.CONFIRMED));
        } catch (TransactionRolledBackException e) {
            //Although undesirable this is actually a valid outcome. See http://jbossts.blogspot.co.uk/2013/01/ws-ba-participant-completion-race.html
            // or the 'Troubleshooting' section of the 'Transactions Development Guide'.
        } finally {
            cancelIfActive(uba);
        }
    }

    /**
     * Tests the scenario where an item is ordered within a business activity that is later cancelled. The test checks
     * that the order was confirmed after invoking placeOrder on the Web service. After cancelling the Business Activity, the
     * work should be compensated and thus the order should no longer be confirmed.
     *
     * @throws Exception if something goes wrong
     */
    @Test
    public void testCancel() throws Exception {

        System.out
                .println("\n\nStarting 'testCancel'. This test invokes a WS within a BA. The BA is later cancelled, which causes these WS call to be compensated.");
        System.out.println("[CLIENT] Creating a new Business Activity");
        UserBusinessActivity uba = UserBusinessActivityFactory.userBusinessActivity();
        try {
            String value = "a book";

            System.out
                    .println("[CLIENT] Beginning Business Activity (All calls to Web services that support WS-BA will be included in this activity)");
            uba.begin();

            System.out.println("[CLIENT] invoking placeOrder('" + value + "') on WS");
            OrderServiceBA client = createClient();
            Integer orderId = client.placeOrder(value);

            Assert.assertTrue("Expected order to be in status pending, but it wasn't", client.getOrderStatus(orderId).equals(OrderStatus.PENDING));

            System.out.println("[CLIENT] Cancelling Business Activity (This will cause the work to be compensated)");
            uba.cancel();

            Assert.assertTrue("Expected order to be cancelled, but it wasn't", client.getOrderStatus(orderId).equals(OrderStatus.CANCELLED));

        } finally {
            cancelIfActive(uba);
        }

    }

    /**
     * Utility method for cancelling a Business Activity if it is currently active.
     *
     * @param uba The User Business Activity to cancel.
     */
    private void cancelIfActive(UserBusinessActivity uba) {

        try {
            uba.cancel();
        } catch (Throwable th2) {
            // do nothing, already closed
        }
    }

    private OrderServiceBA createClient() throws MalformedURLException {

        URL wsdlLocation = new URL("http://localhost:8080/test/OrderServiceBAService/OrderServiceBA?wsdl");
        QName serviceName = new QName("http://www.jboss.org/as/quickstarts/helloworld/wsba/participantcompletion/order",
                "OrderServiceBAService");
        QName portName = new QName("http://www.jboss.org/as/quickstarts/helloworld/wsba/participantcompletion/order",
                "OrderServiceBA");

        Service service = Service.create(wsdlLocation, serviceName);
        OrderServiceBA order = service.getPort(portName, OrderServiceBA.class);

        /*
         * Add client handler chain so that XTS can add the transaction context to the SOAP messages.
         *
         * This will be automatically added by the TXFramework in the future.
         */
        BindingProvider bindingProvider = (BindingProvider) order;
        List<Handler> handlers = new ArrayList<Handler>(1);
        handlers.add(new JaxWSHeaderContextProcessor());
        bindingProvider.getBinding().setHandlerChain(handlers);

        return order;
    }
}
