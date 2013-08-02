package org.jboss.narayana.quickstarts.compensationsApi.travel;

/**
 * @author paul.robinson@redhat.com 18/09/2013
 */
public class TravelBookingResult {

    private Integer hotelBookingId;
    private Integer taxi1BookingId;
    private Integer taxi2BookingId;

    public TravelBookingResult(Integer hotelBookingId, Integer taxi1BookingId, Integer taxi2BookingId) {

        this.hotelBookingId = hotelBookingId;
        this.taxi1BookingId = taxi1BookingId;
        this.taxi2BookingId = taxi2BookingId;
    }

    public Integer getHotelBookingId() {

        return hotelBookingId;
    }

    public Integer getTaxi1BookingId() {

        return taxi1BookingId;
    }

    public Integer getTaxi2BookingId() {

        return taxi2BookingId;
    }
}
