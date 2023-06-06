package org.jboss.narayana.quickstarts.compensationsApi.taxi1.jaxws;

import jakarta.jws.WebMethod;
import jakarta.jws.WebService;
import java.util.Date;

/**
 * Interface implemented by Taxi1Service Web service.
 *
 * @author paul.robinson@redhat.com, 2011-12-21
 */
@WebService(name = "Taxi1Service", targetNamespace = "http://www.jboss.org/as/quickstarts/compensationsApi/travel/taxi1")
public interface Taxi1Service {

    /**
     * Make the booking
     *
     * @param name Name of person making the booking
     * @param date the date of the booking
     */
    @WebMethod
    public Integer makeBooking(String name, Date date);

}