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
/*
 * RestaurantServiceAT.java
 *
 * Copyright (c) 2003, 2004 Arjuna Technologies Ltd
 *
 * $Id: RestaurantServiceAT.java,v 1.3 2004/12/01 16:26:44 kconner Exp $
 *
 */
package org.jboss.narayana.quickstarts.wsat.jtabridge;

import org.jboss.narayana.quickstarts.wsat.jtabridge.jaxws.RestaurantServiceAT;
import org.jboss.narayana.txframework.api.annotation.service.ServiceRequest;
import org.jboss.narayana.txframework.api.annotation.transaction.Transactional;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * An adapter class that exposes the RestaurantManager business API as a transactional Web Service.
 *
 * @author paul.robinson@redhat.com, 2012-01-04
 */
@Transactional //By default bridge from WS-AT to JTA
@Stateless
@Remote(RestaurantServiceAT.class)
@WebService(serviceName = "RestaurantServiceATService", portName = "RestaurantServiceAT", name = "RestaurantServiceAT", targetNamespace = "http://www.jboss.org/narayana/quickstarts/wsat/simple/Restaurant")
@SOAPBinding(style = SOAPBinding.Style.RPC)
@TransactionAttribute(TransactionAttributeType.MANDATORY) // default is REQUIRED
public class RestaurantServiceATImpl implements RestaurantServiceAT {

    private static final int ENTITY_ID = 1;

    @PersistenceContext
    protected EntityManager em;

    /**
     * Book a number of seats in the restaurant. This is done by updating the BookingCountEntity within a JTA transaction. The JTA transaction
     * was automatically bridged from the WS-AT transaction.
     */
    @WebMethod
    @ServiceRequest
    public void makeBooking(int numSeats) {

        System.out.println("[SERVICE] Restaurant service invoked to make a booking for '" + numSeats + "'");

        // invoke the backend business logic:
        System.out.println("[SERVICE] Using the JPA Entity Manager to update the BookingCountEntity within a JTA transaction");

        BookingCountEntity entity = lookupBookingCountEntity();
        entity.addBookings(1);
        em.merge(entity);
    }

    @WebMethod
    public int getBookingCount() {
        System.out.println("[SERVICE] getBookingCount() invoked");
        BookingCountEntity bookingCountEntity = lookupBookingCountEntity();
        if (bookingCountEntity == null) {
            return -1;
        }
        return bookingCountEntity.getBookingCount();
    }

    @WebMethod
    public void resetBookingCount() {
        BookingCountEntity entity = lookupBookingCountEntity();
        entity.setBookingCount(0);
        em.merge(entity);
    }

    private BookingCountEntity lookupBookingCountEntity() {
        BookingCountEntity entity = em.find(BookingCountEntity.class, ENTITY_ID);
        if (entity == null) {
            entity = new BookingCountEntity();
            em.persist(entity);
        }
        return entity;
    }

}
