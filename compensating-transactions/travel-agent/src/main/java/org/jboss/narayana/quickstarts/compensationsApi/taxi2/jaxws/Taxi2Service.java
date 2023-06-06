package org.jboss.narayana.quickstarts.compensationsApi.taxi2.jaxws;

import org.jboss.narayana.quickstarts.compensationsApi.taxi2.BookingStatus;

import jakarta.jws.WebMethod;
import jakarta.jws.WebService;
import java.util.Date;

/**
 * Interface implemented by Taxi2Service Web service.
 *
 * @author paul.robinson@redhat.com, 2011-12-21
 */
@WebService(name = "Taxi2Service", targetNamespace = "http://www.jboss.org/as/quickstarts/compensationsApi/travel/taxi2")
public interface Taxi2Service {

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