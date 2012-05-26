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
 * TaxiServiceAT.java
 *
 * Copyright (c) 2003, 2004 Arjuna Technologies Ltd
 *
 * $Id: TaxiServiceAT.java,v 1.3 2004/12/01 16:26:44 kconner Exp $
 *
 */
package org.jboss.narayana.quickstarts.restat.taxi;

import org.jboss.narayana.txframework.api.annotation.lifecycle.at.Commit;
import org.jboss.narayana.txframework.api.annotation.lifecycle.at.Prepare;
import org.jboss.narayana.txframework.api.annotation.lifecycle.at.Rollback;
import org.jboss.narayana.txframework.api.annotation.management.DataManagement;
import org.jboss.narayana.txframework.api.annotation.service.ServiceRequest;
import org.jboss.narayana.txframework.api.annotation.transaction.Transactional;

import javax.ejb.Stateless;
import javax.ws.rs.core.Response;
import java.util.Map;

/**
 * An adapter class that exposes the TaxiManager business API as a transactional Web Service.
 *
 * @author paul.robinson@redhat.com, 2012-01-04
 */
@Stateless
@Transactional
public class TaxiServiceATImpl implements TaxiServiceAT {

    private MockTaxiManager mockTaxiManager = MockTaxiManager.getSingletonInstance();

    @DataManagement
    Map dataControl;

    private static final String BOOKING_ID_KEY = "BOOKING_ID_KEY";

    /**
     * Book a number of seats in the taxi. Enrols a Participant, then passes the call through to the business logic.
     */
    @ServiceRequest
    public Response makeBooking() {

        System.out.println("[SERVICE] taxi service invoked to make a booking");

        // invoke the backend business logic:
        System.out.println("[SERVICE] Invoking the back-end business logic");
        String bookingId = mockTaxiManager.makeBooking();
        dataControl.put(BOOKING_ID_KEY, bookingId);

        return Response.ok().build();
    }

    /**
     * obtain the number of existing bookings
     *
     * @return the number of current bookings
     */
    public Response getBookingCount() {
        Integer bookingCount =  mockTaxiManager.getBookingCount();
        return Response.ok(bookingCount).build();
    }

    /**
     * Reset the booking count to zero
     * <p/>
     * Note: To simplify this example, this method is not part of the compensation logic, so will not be undone if the AT is
     * compensated. It can also be invoked outside of an active AT.
     */
    public Response reset() {
        mockTaxiManager.reset();
        return Response.ok().build();
    }


    /**
     * Invokes the prepare step of the business logic, reporting activity and outcome.
     *
     * @return Prepared where possible, Aborted where necessary.
     */
    @Prepare
    public Boolean prepare() {

        String bookingId = (String) dataControl.get(BOOKING_ID_KEY);

        // Log the event and invoke the prepare operation
        // on the back-end logic.
        System.out.println("[SERVICE] Prepare called on participant, about to prepare the back-end resource");
        boolean success = mockTaxiManager.prepare(bookingId);

        if (success) {
            System.out.println("[SERVICE] back-end resource prepared, participant votes prepared");
        } else {
            System.out.println("[SERVICE] back-end resource failed to prepare, participant votes aborted");
        }
        return success;
    }

    /**
     * Invokes the commit step of the business logic.
     */
    @Commit
    public void commit() {
        String bookingId = (String) dataControl.get(BOOKING_ID_KEY);
        // Log the event and invoke the commit operation
        // on the backend business logic.
        System.out.println("[SERVICE] all participants voted 'prepared', so coordinator tells the participant to commit");
        mockTaxiManager.commit(bookingId);
    }

    /**
     * Invokes the rollback operation on the business logic.
     */
    @Rollback
    public void rollback() {
        String bookingId = (String) dataControl.get(BOOKING_ID_KEY);
        // Log the event and invoke the rollback operation
        // on the backend business logic.
        System.out.println("[SERVICE] one or more participants voted 'aborted' or a failure occurred, so coordinator tells the participant to rollback");
        mockTaxiManager.rollback(bookingId);
    }
}
