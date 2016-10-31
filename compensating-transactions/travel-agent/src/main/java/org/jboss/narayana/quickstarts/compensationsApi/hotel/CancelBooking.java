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
package org.jboss.narayana.quickstarts.compensationsApi.hotel;

import org.jboss.narayana.compensations.api.CompensationHandler;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

/**
 * This is the compensation handler to be invoke if the transaction is cancelled. In this example the compensation handler will
 * update the booking from state 'pending' to 'cancelled'.
 * <p/>
 * It uses the injected 'BookingData' to find out which booking is being cancelled.
 *
 * @author paul.robinson@redhat.com 02/08/2013
 */
public class CancelBooking implements CompensationHandler {

    @Inject
    BookingData bookingData;

    @PersistenceContext
    protected EntityManager em;

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void compensate() {

        System.out.println("[HOTEL SERVICE] compensate (The participant should undo any work done within this transaction)");

        HotelBooking booking = em.find(HotelBooking.class, bookingData.getBookingId());
        booking.setStatus(BookingStatus.CANCELLED);
        em.merge(booking);
    }
}
