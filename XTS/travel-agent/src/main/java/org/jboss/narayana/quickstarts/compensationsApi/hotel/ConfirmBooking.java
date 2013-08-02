package org.jboss.narayana.quickstarts.compensationsApi.hotel;

import org.jboss.narayana.compensations.api.ConfirmationHandler;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

/**
 * This is the confirmation handler to be invoke if the compensation-based transaction is closed (completed successfully).
 * In this example the confirmation handler will update the booking from state 'pending' to 'confirmed'.
 * <p/>
 * It uses the injected 'BookingData' to find out which booking is being confirmed.
 *
 * @author paul.robinson@redhat.com 02/08/2013
 */
public class ConfirmBooking implements ConfirmationHandler {

    @Inject
    BookingData bookingData;

    @PersistenceContext
    protected EntityManager em;

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void confirm() {

        System.out.println("[HOTEL SERVICE] Confirm (The participant should confirm any work done within this transaction)");

        HotelBooking booking = em.find(HotelBooking.class, bookingData.getBookingId());
        booking.setStatus(BookingStatus.CONFIRMED);
        em.merge(booking);
    }
}
