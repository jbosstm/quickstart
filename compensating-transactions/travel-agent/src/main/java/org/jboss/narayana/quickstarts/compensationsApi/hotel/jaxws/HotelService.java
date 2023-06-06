package org.jboss.narayana.quickstarts.compensationsApi.hotel.jaxws;

import org.jboss.narayana.quickstarts.compensationsApi.hotel.BookingStatus;

import jakarta.jws.WebMethod;
import jakarta.jws.WebService;
import java.util.Date;

/**
 * Interface implemented by HotelService Web service.
 *
 * @author paul.robinson@redhat.com, 2011-12-21
 */
@WebService(name = "HotelService", targetNamespace = "http://www.jboss.org/as/quickstarts/compensationsApi/travel/hotel")
public interface HotelService {

    /**
     * Make the booking
     *
     * @param name Name of person making the booking
     * @param date the date of the booking
     */
    @WebMethod
    public Integer makeBooking(String name, Date date);

    /**
     * Get the status of the booking
     *
     * @param bookingId the ID of the booking to query
     * @return BookingStatus the status of the booking
     */
    @WebMethod
    public BookingStatus getBookingStatus(Integer bookingId);

}