package org.jboss.narayana.quickstarts.wsat.jtabridge.first.jaxws;

import jakarta.ejb.Remote;
import jakarta.jws.WebMethod;
import jakarta.jws.WebService;
import jakarta.jws.soap.SOAPBinding;

/**
 * Interface to a simple First. Provides simple methods to manipulate bookings.
 * 
 * @author paul.robinson@redhat.com, 2012-01-04
 */
@WebService(name = "FirstServiceAT", targetNamespace = "http://www.jboss.org/narayana/quickstarts/wsat/simple/first")
@SOAPBinding(style = SOAPBinding.Style.RPC)
@Remote
public interface FirstServiceAT {

    /**
     * Create a new booking
     */
    @WebMethod
    public void incrementCounter(int numSeats);

    /**
     * obtain the number of existing bookings
     * 
     * @return the number of current bookings
     */
    @WebMethod
    public int getFirstCounter();

    /**
     * obtain the number of existing bookings
     *
     * @return the number of current bookings
     */
    @WebMethod
    public int getSecondCounter();

    /**
     * Reset the booking count to zero
     */
    @WebMethod
    public void resetCounter();

}