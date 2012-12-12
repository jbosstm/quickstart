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
package org.jboss.narayana.quickstarts.wsba.participantcompletion.simple;

import org.jboss.narayana.quickstarts.wsba.participantcompletion.simple.jaxws.OrderServiceBA;
import org.jboss.narayana.txframework.api.annotation.lifecycle.ba.Cancel;
import org.jboss.narayana.txframework.api.annotation.lifecycle.ba.Compensate;
import org.jboss.narayana.txframework.api.annotation.service.ServiceRequest;
import org.jboss.narayana.txframework.api.annotation.transaction.Compensatable;
import org.jboss.narayana.txframework.api.configuration.transaction.CompletionType;
import org.jboss.narayana.txframework.api.management.TXDataMap;
import org.jboss.narayana.txframework.api.management.WSBATxControl;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.servlet.annotation.WebServlet;
import java.util.Map;

/**
 * A simple Web service that accepts product orders and sends confirmation emails. If the BA is cancelled, then a cancellation EMail is sent.
 *
 * @author Paul Robinson (paul.robinson@redhat.com)
 */
@Compensatable(completionType = CompletionType.PARTICIPANT)
@WebService(serviceName = "OrderServiceBAService", portName = "OrderServiceBA", name = "OrderServiceBA", targetNamespace = "http://www.jboss.org/as/quickstarts/helloworld/wsba/participantcompletion/order")
@SOAPBinding(style = SOAPBinding.Style.RPC)
@Stateless
@Remote(OrderServiceBA.class)
public class OrderServiceBAImpl implements OrderServiceBA {

    /*
     * The @TxManagement injection provides a control to the running transaction. It can be used to notify the coordinator of important events. For example if the
     * participant has completed it's work or if the participant is unable to complete.
     */
    @Inject
    public WSBATxControl txControl;

    /*
        The @DataManagement injection provides a map that that is isolated to the transaction participant. This allows the service to store data that
        can be retrieved when the protocol lifecycle methods are invoked by the coordinator (those annotated with @Compensate, @Cancel, etc).
        The Map is isolated within a particular transaction; therefore it is safe for multiple transactions to use this map without seeing each others' data.
     */
    @Inject
    private TXDataMap<String, String> txDataMap;

    /*
     * This flag is used by the test to check whether the order was confirmed or cancelled. It is not thread safe as the same instance is used by all threads.
     * As this is a simple example and the variable is only used by the (sequentially ran) test, we are not too concerned about this.
     */
    private static boolean orderConfirmed = false;

    /**
     * Places an order for the specified item. As this is a simple example, all the method does is attempt to send the confirmation email.
     *
     * @param item Item to order
     * @throws OrderServiceException if an error occurred when making the order
     */
    @WebMethod
    // This annotation is used by the TXFramework to know that this method participates in the BA
    @ServiceRequest
    public void placeOrder(String item) throws OrderServiceException {

        System.out.println("[SERVICE] invoked placeOrder('" + item + "')");

        //Store value for use by compensation.
        txDataMap.put("value", item);

        /*
         * this service employs the participant completion protocol which means it has completed the work for this BA.
         * If the local changes (emailing the order confirmation to the customer) succeeded, we notify the coordinator that we have
         * completed. Otherwise, we notify the coordinator that we cannot complete. If any other participant fails or the client
         * decides to cancel we can rely upon being told to compensate.
         */
        System.out.println("[SERVICE] Attempt to email an order confirmation, if successful notify the coordinator that we have completed our work");

        boolean success = EmailSender.sendEmail("Your order is now confirmed for the following item: '" + item + "'");

        if (success) {
            try {
                System.out.println("[SERVICE] Email sent successfully, notifying coordinator of completion");
                // tell the coordinator manager we have finished our work
                txControl.completed();
                orderConfirmed = true;
            } catch (Exception e) {
                /*
                 * Failed to notify the coordinator that we have finished our work. Compensate the work and throw an Exception
                 * to notify the client that the order operation failed.
                 */
                doCompensate();
                System.err.println("[SERVICE]  'completed' callback failed");
                throw new OrderServiceException("Error when notifying the coordinator that the work is completed", e);
            }
        } else {
            try {
                /*
                 * tell the participant manager we cannot complete. this will force the activity to fail
                 */
                System.out.println("[SERVICE] Email send failed, notifying coordinator that we cannot complete");
                txControl.cannotComplete();
            } catch (Exception e) {
                System.err.println("'cannotComplete' callback failed");
                throw new OrderServiceException("Error when notifying the coordinator that the work is cannot be completed", e);
            }
            throw new OrderServiceException("Email send failed, order cancelled");
        }

    }

    /**
     * The BA has canceled, and the participant should undo any work. The participant cannot have informed the
     * coordinator that it has completed.
     */
    @Cancel
    public void cancel() {
        System.out.println("[SERVICE] @Cancel (The participant should compensate any work done within this BA)");
        doCompensate();
    }

    /**
     * The BA has cancelled. The participant previously informed the coordinator that it had finished work but could
     * compensate later if required, and it is now requested to do so.
     */
    @Compensate
    public void compensate() {
        System.out.println("[SERVICE] @Compensate");
        doCompensate();
    }

    private void doCompensate() {
        String item = (String) txDataMap.get("value");
        EmailSender.sendEmail("Unfortunately, we have had to cancel your order for item '" + item + "'");
        orderConfirmed = false;
    }


    /**
     * Query to check if the order ws confirmed or cancelled. This is used by the tests.
     *
     * @return true if the value was present, false otherwise.
     */
    @WebMethod
    public boolean orderConfirmed() {
        return orderConfirmed;
    }

    /**
     * reset the orderConfirmed flag. This is used by the tests.
     * <p/>
     * Note: To simplify this example, this method is not part of the compensation logic, so will not be undone if the BA is
     * compensated. It can also be invoked outside of an active BA.
     */
    @WebMethod
    public void reset() {
        orderConfirmed = false;
    }
}
