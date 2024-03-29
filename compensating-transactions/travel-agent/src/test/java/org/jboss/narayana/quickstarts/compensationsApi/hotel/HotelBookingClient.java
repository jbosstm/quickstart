package org.jboss.narayana.quickstarts.compensationsApi.hotel;

import org.jboss.narayana.compensations.api.Compensatable;
import org.jboss.narayana.compensations.api.CompensationManager;
import org.jboss.narayana.quickstarts.compensationsApi.hotel.jaxws.HotelService;

import jakarta.inject.Inject;
import javax.xml.namespace.QName;
import jakarta.xml.ws.Service;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

/**
 * @author paul.robinson@redhat.com 18/09/2013
 */
public class HotelBookingClient {

    @Inject
    private CompensationManager compensationManager;

    private HotelService client;

    private Integer lastBookingId;

    public HotelBookingClient() {

        this.client = createWebServiceClient();
    }

    /**
     * This method will begin a new Compensating Transaction (see the @Compensatable annotation) and then invoke the
     * 'makeBooking' method on the Hotel Web Service. The compensating transaction context is automatically
     * distributed to the Hotel Service.
     *
     * The service can fail the compensating transaction if told to do so. It does this by telling the CompensationManager
     * that the transaction can only be compensated.
     *
     * @param name
     * @param date
     * @param fail
     * @return
     */
    @Compensatable(distributed = true)
    public Integer makeBooking(String name, Date date, boolean fail) {

        lastBookingId = client.makeBooking(name, date);

        if (fail) {
            compensationManager.setCompensateOnly();
        }

        /**
         * This is here in order to eliminate participant completion race condition.
         * See these blog posts:
         *      http://jbossts.blogspot.co.uk/2013/01/ws-ba-participant-completion-race.html
         *      http://jbossts.blogspot.co.uk/2013/01/ws-ba-participant-completion-race_30.html
         * And JIRA:
         *      https://issues.jboss.org/browse/JBTM-1718
         */
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            // Ignore
        }

        return lastBookingId;
    }

    public BookingStatus getLastBookingStatus() {

        return client.getBookingStatus(lastBookingId);
    }


    private HotelService createWebServiceClient() {

        try {
            URL wsdlLocation = new URL("http://localhost:8080/test/HotelServiceService?wsdl");
            QName serviceName = new QName("http://www.jboss.org/as/quickstarts/compensationsApi/travel/hotel",
                    "HotelServiceService");
            QName portName = new QName("http://www.jboss.org/as/quickstarts/compensationsApi/travel/hotel",
                    "HotelService");

            Service service = Service.create(wsdlLocation, serviceName);
            return service.getPort(portName, HotelService.class);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error creating Web Service client", e);
        }
    }

}