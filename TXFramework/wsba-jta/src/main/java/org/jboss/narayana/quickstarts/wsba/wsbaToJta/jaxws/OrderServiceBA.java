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
package org.jboss.narayana.quickstarts.wsba.wsbaToJta.jaxws;

import org.jboss.narayana.quickstarts.wsba.wsbaToJta.OrderStatus;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

/**
 * Interface implemented by OrderServiceBA Web service and Client stub.
 *
 * @author paul.robinson@redhat.com, 2011-12-21
 */
@WebService(name = "OrderServiceBA", targetNamespace = "http://www.jboss.org/as/quickstarts/helloworld/wsba/participantcompletion/order")
@SOAPBinding(style = SOAPBinding.Style.RPC)
public interface OrderServiceBA {

    /**
     * Place an order
     *
     * @param item Item to order
     */
    @WebMethod
    public Integer placeOrder(String item);

    /**
     * Check to see if order was confirmed and not canceled.
     *
     * @return true if the order was confirmed, false if it was not confirmed or if it was later cancelled
     */
    @WebMethod
    public OrderStatus getOrderStatus(Integer orderId);

}
