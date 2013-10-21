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
package org.jboss.narayana.quickstarts.compensationsApi.hotel;

import org.jboss.narayana.compensations.api.Compensatable;
import org.jboss.narayana.compensations.api.CompensationTransactionType;
import org.jboss.narayana.compensations.api.TxCompensate;
import org.jboss.narayana.compensations.api.TxConfirm;
import org.jboss.narayana.quickstarts.compensationsApi.hotel.jaxws.HotelService;

import javax.inject.Inject;
import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.Date;

/**
 * A simple Web service that accepts bookings and adds them to a database.
 * <p/>
 * If the transaction is cancelled, the activity is compensated, by marking the booking as cancelled in the database.
 * <p/>
 * If the transaction is closed, the booking is marked as confirmed in the database.
 *
 * @author Paul Robinson (paul.robinson@redhat.com)
 */
@WebService(serviceName = "HotelServiceService", portName = "HotelService", name = "HotelService", targetNamespace = "http://www.jboss.org/as/quickstarts/compensationsApi/travel/hotel")
public class HotelServiceImpl implements HotelService {

    @PersistenceContext
    protected EntityManager em;

    /*
        The BookingData injection provides an instance that is isolated to the transaction participant. This allows the service to store data that
        can be retrieved when the compensation or confirmation handlers are invoked.
        The instance is isolated within a particular transaction; therefore it is safe for multiple transactions to use this injection without seeing each others' data.
     */
    @Inject
    private BookingData bookingData;

    /**
     * Places a booking. As this is a simple example, all the method does is mark the booking as pending in the database.
     * <p/>
     * The @TxCompensate annotation specifies which compensation handler to invoke if the transaction is cancelled. In this example the compensation handler will
     * update the booking from state 'pending' to 'cancelled'.
     * <p/>
     * The @TxConfirm annotation specifies which confirmation handler to invoke if the transaction is closed (completed successfully). In this example the confirmation handler will
     * update the booking from state 'pending' to 'confirmed'.
     * <p/>
     * <p/>
     * This method is invoked within a new JTA transaction that is automatically committed if the method returns successfully.
     *
     * @param name The name of the person making the booking
     * @param date The date of the booking.
     */
    @Compensatable(CompensationTransactionType.MANDATORY)
    @TxCompensate(CancelBooking.class)
    @TxConfirm(ConfirmBooking.class)
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    @WebMethod
    public Integer makeBooking(String name, Date date) {

        System.out.println("[SERVICE] invoked makeBooking('" + name + "', '" + date + "')");

        HotelBooking booking = new HotelBooking();
        booking.setName(name);
        booking.setDate(date);
        em.persist(booking);

        //Store the booking ID for use by compensation or confirm.
        bookingData.setBookingId(booking.getId());
        return booking.getId();
    }

    /**
     * Query to find out the current status of the booking. This is used by the tests.
     *
     * @return BookingStatus the status of the booking.
     */
    @WebMethod
    public BookingStatus getBookingStatus(Integer bookingId) {

        HotelBooking booking = em.find(HotelBooking.class, bookingId);

        if (booking != null) {
            return booking.getStatus();
        } else {
            return BookingStatus.NOT_EXIST;
        }
    }
}
