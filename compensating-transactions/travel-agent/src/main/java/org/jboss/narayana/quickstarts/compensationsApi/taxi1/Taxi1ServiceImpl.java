package org.jboss.narayana.quickstarts.compensationsApi.taxi1;

import org.jboss.narayana.compensations.api.Compensatable;
import org.jboss.narayana.compensations.api.CompensationTransactionType;
import org.jboss.narayana.quickstarts.compensationsApi.taxi1.jaxws.Taxi1Service;

import jakarta.jws.WebMethod;
import jakarta.jws.WebService;
import java.util.Date;

/**
 * A simple Web service that accepts bookings and adds them to a database. This service is always fully-booked.
 *
 * @author Paul Robinson (paul.robinson@redhat.com)
 */
@WebService(serviceName = "Taxi1ServiceService", portName = "Taxi1Service", name = "Taxi1Service", targetNamespace = "http://www.jboss.org/as/quickstarts/compensationsApi/travel/taxi1")
public class Taxi1ServiceImpl implements Taxi1Service {

    /**
     * Places a booking. This service always fails to make a booking. It communicates this to the caller of the service
     * by throwing a BookingUnavailableRuntimeException.
     * <p/>
     * Notice that the @Compensatable annotation is configured so as not to cancel the enclosing compensation-based
     * transaction when the BookingUnavailableRuntimeException exception is thrown. This is because the failure of this
     * service may be handled by the caller. The caller will receive an exception, so can decide whether to cancel
     * the compensation-based transaction or whether to handle in some other way so as to continue to make progress.
     * <p/>
     * NOTE: the compensation handler will not be invoked. As the business method raised the exception, it is expected
     * that it leaves the application in a consistent state.
     * <p/>
     * If the compensation-based transaction had been canceled, the hotel booking would be lost. Therefore, in this
     * example, a booking with an alternative Taxi company is attempted.
     *
     * @param name The name of the person making the booking
     * @param date The date of the booking.
     */
    @Compensatable(value = CompensationTransactionType.MANDATORY, dontCancelOn = BookingUnavailableRuntimeException.class)
    @WebMethod
    public Integer makeBooking(String name, Date date) {

        throw new BookingUnavailableRuntimeException("Fully Booked");
    }
}