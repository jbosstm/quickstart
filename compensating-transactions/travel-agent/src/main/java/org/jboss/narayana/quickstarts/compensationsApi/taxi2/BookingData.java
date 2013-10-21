package org.jboss.narayana.quickstarts.compensationsApi.taxi2;

import org.jboss.narayana.compensations.api.CompensationScoped;

import java.io.Serializable;

/**
 * This bean is used to store state in the business method and make it available when the confirmation and compensation
 * handlers are invoked.
 *
 * @author paul.robinson@redhat.com 02/08/2013
 */
@CompensationScoped
public class BookingData implements Serializable {

    Integer bookingId;

    public Integer getBookingId() {

        return bookingId;
    }

    public void setBookingId(Integer bookingId) {

        this.bookingId = bookingId;
    }
}
