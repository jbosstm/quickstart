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

import org.jboss.narayana.quickstarts.wsba.wsbaToJta.jaxws.OrderServiceBA;
import org.jboss.narayana.txframework.api.annotation.lifecycle.ba.Close;
import org.jboss.narayana.txframework.api.annotation.lifecycle.ba.Compensate;
import org.jboss.narayana.txframework.api.annotation.lifecycle.ba.Completes;
import org.jboss.narayana.txframework.api.annotation.service.ServiceRequest;
import org.jboss.narayana.txframework.api.annotation.transaction.Compensatable;
import org.jboss.narayana.txframework.api.configuration.transaction.CompletionType;
import org.jboss.narayana.txframework.api.management.TXDataMap;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * A simple Web service that accepts purchase orders and adds them to a database.
 * <p/>
 * If the BA is cancelled, the activity is compensated, by marking the order marked as cancelled in the database.
 * <p/>
 * If the BA is closed, the order is marked as confirmed in the database.
 *
 * @author Paul Robinson (paul.robinson@redhat.com)
 */
@Stateless
@Compensatable(completionType = CompletionType.PARTICIPANT)
@WebService(serviceName = "OrderServiceBAService", portName = "OrderServiceBA", name = "OrderServiceBA", targetNamespace = "http://www.jboss.org/as/quickstarts/helloworld/wsba/participantcompletion/order")
@SOAPBinding(style = SOAPBinding.Style.RPC)
public class OrderServiceBAImpl implements OrderServiceBA {

    @PersistenceContext
    protected EntityManager em;

    /*
        The @TXDataMap injection provides a map that that is isolated to the transaction participant. This allows the service to store data that
        can be retrieved when the protocol lifecycle methods are invoked by the coordinator (those annotated with @Compensate, @Close, etc).
        The Map is isolated within a particular transaction; therefore it is safe for multiple transactions to use this map without seeing each others' data.
     */
    @Inject
    private TXDataMap<String, Integer> txDataMap;

    /**
     * Places an order for the specified item. As this is a simple example, all the method does is add it to the database.
     * <p/>
     * This service employs the participant completion protocol which means it must notify the coordinator when it has completed its work.
     * The method is annotated with '@Completes' which causes this notification to be sent automatically, when the method returns.
     * If the local changes (adding the order to the database) succeeded, the coordinator is notified that we have
     * completed. Otherwise, the coordinator is notified that we cannot complete. If any other participant fails or the client
     * decides to cancel we can rely upon being told to compensate.
     * <p/>
     * This method is invoked within a new JTA transaction that is automatically committed if the method returns successfully.
     *
     * @param item Item to order
     */
    @WebMethod
    @ServiceRequest // This annotation is used by the TXFramework to know that this method participates in the BA
    @Completes
    public Integer placeOrder(String item) {

        System.out.println("[SERVICE] invoked placeOrder('" + item + "')");

        PurchaseOrder purchaseOrder = new PurchaseOrder(item);
        em.persist(purchaseOrder);

        //Store order ID for use by compensation or close.
        txDataMap.put("orderId", purchaseOrder.getId());
        return purchaseOrder.getId();
    }

    /**
     * The BA was successful, so update the order and mark it as 'confirmed'.
     * <p/>
     * This method is invoked within a new JTA transaction that is automatically committed if the method returns successfully.
     * <p/>
     * Note, that this method may be invoked more than once, therefore the logic needs to cope with this.
     */
    @Close
    public void confirm() {

        System.out.println("[SERVICE] @Confirm (The participant should confirm any work done within this BA)");
        Integer orderId = txDataMap.get("orderId");

        PurchaseOrder purchaseOrder = em.find(PurchaseOrder.class, orderId);
        purchaseOrder.setStatus(OrderStatus.CONFIRMED);
        em.merge(purchaseOrder);
    }

    /**
     * The BA was unsuccessful, so update the order and mark it as 'cancelled'.
     * <p/>
     * This method is invoked within a new JTA transaction that is automatically committed if the method returns successfully.
     * <p/>
     * Note, that this method may be invoked more than once, therefore the logic needs to cope with this.
     */
    @Compensate
    public void compensate() {

        System.out.println("[SERVICE] @Compensate (The participant should compensate any work done within this BA)");
        Integer orderId = txDataMap.get("orderId");

        PurchaseOrder purchaseOrder = em.find(PurchaseOrder.class, orderId);
        purchaseOrder.setStatus(OrderStatus.CANCELLED);
        em.merge(purchaseOrder);
    }

    /**
     * Query to find out the current status of the order. This is used by the tests.
     *
     * @return OrderStatus the status of the order.
     */
    @WebMethod
    public OrderStatus getOrderStatus(Integer orderId) {

        PurchaseOrder purchaseOrder = em.find(PurchaseOrder.class, orderId);

        if (purchaseOrder != null) {
            return purchaseOrder.getStatus();
        } else {
            return OrderStatus.NOT_EXIST;
        }
    }
}
