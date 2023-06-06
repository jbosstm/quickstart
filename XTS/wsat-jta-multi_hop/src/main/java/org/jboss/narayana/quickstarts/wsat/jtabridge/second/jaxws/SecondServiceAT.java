package org.jboss.narayana.quickstarts.wsat.jtabridge.second.jaxws;

import jakarta.ejb.Remote;
import jakarta.jws.WebMethod;
import jakarta.jws.WebService;
import jakarta.jws.soap.SOAPBinding;

/**
 * Interface to a simple Second. Provides simple methods to manipulate bookings.
 * 
 * @author paul.robinson@redhat.com, 2012-01-04
 */
@WebService(name = "SecondServiceAT", targetNamespace = "http://www.jboss.org/narayana/quickstarts/wsat/simple/second")
@SOAPBinding(style = SOAPBinding.Style.RPC)
@Remote
public interface SecondServiceAT {

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
    public int getCounter();

    /**
     * Reset the booking count to zero
     */
    @WebMethod
    public void resetCounter();

}