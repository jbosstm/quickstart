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
package org.jboss.narayana.quickstarts.wsba.simple;

import org.jboss.narayana.quickstarts.wsba.simple.jaxws.OrderServiceBA;
import org.jboss.narayana.txframework.api.annotation.lifecycle.ba.Compensate;
import org.jboss.narayana.txframework.api.annotation.lifecycle.ba.Completes;
import org.jboss.narayana.txframework.api.annotation.service.ServiceRequest;
import org.jboss.narayana.txframework.api.annotation.transaction.Compensatable;
import org.jboss.narayana.txframework.api.configuration.transaction.CompletionType;
import org.jboss.narayana.txframework.api.management.TXDataMap;

import javax.inject.Inject;
import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.servlet.annotation.WebServlet;

/**
 * A simple Web service that accepts product orders and sends confirmation emails. If the BA is compensated, a cancellation email is sent.
 *
 * @author Paul Robinson (paul.robinson@redhat.com)
 */
@Compensatable(completionType = CompletionType.PARTICIPANT)
@WebService(serviceName = "OrderServiceBAService", portName = "OrderServiceBA", name = "OrderServiceBA", targetNamespace = "http://www.jboss.org/as/quickstarts/helloworld/wsba/participantcompletion/order")
@SOAPBinding(style = SOAPBinding.Style.RPC)
@WebServlet("/OrderServiceBA")
public class OrderServiceBAImpl implements OrderServiceBA {

    /*
        The @TXDataMap injection provides a map that that is isolated to both the transaction and this participant. This allows the service to store data that
        can be retrieved when the protocol lifecycle methods are invoked by the coordinator (those annotated with @Compensate, @Cancel, etc).
        The Map is isolated within a particular transaction; therefore it is safe for multiple transactions to use this map without seeing each others' data.
        The data is automatically removed after the transaction has ended.
     */
    @Inject
    private TXDataMap<String, String> txDataMap;

    /**
     * Places an order for the specified item. As this is a simple example, all the method does is attempt to send the confirmation email.
     *
     * The method is annotated with '@Completes' which means that providing the method doesn't throw an Exception, the coordinator will
     * be automatically notified that the participant completed.
     *
     * @param emailAddress The email address of the person making the order.
     * @param item Item to be purchased
     * @throws OrderServiceException if an error occurred when making the order. In this case if an invalid email address is provided.
     */
    @WebMethod
    @ServiceRequest // This annotation is used by the TXFramework to know that this method participates in the BA
    @Completes
    public void placeOrder(String emailAddress, String item) throws OrderServiceException {

        System.out.println("[SERVICE] invoked placeOrder('" + item + "')");

        /*
         * Do some work to create the order. For example, create an entry in a database.
         *
         * This has been left out of this example to keep it simple.
         */
        String orderId = "someId"; //Normally generated when creating the order. Maybe the generated PK of the Order record.

        /*
         * Store data for use by compensation.
         */
        txDataMap.put("emailAddress", emailAddress);
        txDataMap.put("orderId", orderId);


        /* If the following fails (due to an invalid email address, in this example) an exception will be thrown and the middleware will notify the coordinator
         * that this participant was unable to complete.
         */
        System.out.println("[SERVICE] Attempt to email an order confirmation. Failure would raise an exception causing the coordinator to be informed that this participant cannot complete.");
        EmailSender.sendEmail(emailAddress, EmailSender.MAIL_TEMPLATE_CONFIRMATION);
    }

    /**
     * The BA has cancelled. The participant previously informed the coordinator that it had finished work but could
     * compensate later if required, and it is now requested to do so.
     */
    @Compensate
    public void compensate() {

        System.out.println("[SERVICE] @Compensate called");

        //Lookup the item Do something to cancel the order, maybe remove it from the database. This is outside the scope of this quickstart.
        String orderId = txDataMap.get("orderId");

        /*
         * Email the customer to notify them that the order ws cancelled.
         */
        String emailAddress = txDataMap.get("emailAddress");
        EmailSender.sendEmail(emailAddress, EmailSender.MAIL_TEMPLATE_CANCELLATION);
    }
}
