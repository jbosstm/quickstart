package org.jboss.narayana.quickstarts.compensationsApi.taxi2;

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

        System.out.println("[TAXI2 SERVICE] compensate (The participant should undo any work done within this transaction)");

        Taxi2Booking booking = em.find(Taxi2Booking.class, bookingData.getBookingId());
        booking.setStatus(BookingStatus.CANCELLED);
        em.merge(booking);
    }
}
