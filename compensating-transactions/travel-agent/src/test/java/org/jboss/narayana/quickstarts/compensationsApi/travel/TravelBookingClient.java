package org.jboss.narayana.quickstarts.compensationsApi.travel;

import org.jboss.narayana.compensations.api.Compensatable;
import org.jboss.narayana.quickstarts.compensationsApi.hotel.jaxws.HotelService;
import org.jboss.narayana.quickstarts.compensationsApi.taxi1.jaxws.Taxi1Service;
import org.jboss.narayana.quickstarts.compensationsApi.taxi2.jaxws.Taxi2Service;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.soap.SOAPFaultException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

/**
 * @author paul.robinson@redhat.com 18/09/2013
 */
public class TravelBookingClient {

    HotelService hotelBookingClient;
    Taxi1Service taxi1BookingClient;
    Taxi2Service taxi2BookingClient;

    public TravelBookingClient() {

        hotelBookingClient = createHotelServiceClient();
        taxi1BookingClient = createTaxi1ServiceClient();
        taxi2BookingClient = createTaxi2ServiceClient();
    }

    @Compensatable
    public TravelBookingResult makeBooking(String name, Date date) {

        Integer hotelBookingID = hotelBookingClient.makeBooking(name, date);

        Integer taxi1BookingID = null;
        Integer taxi2BookingID = null;

        try {
            taxi1BookingID = taxi1BookingClient.makeBooking(name, date);
        } catch (SOAPFaultException e) {
            taxi2BookingID = taxi2BookingClient.makeBooking(name, date);
        }

        return new TravelBookingResult(hotelBookingID, taxi1BookingID, taxi2BookingID);
    }

    private HotelService createHotelServiceClient() {

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

    private Taxi1Service createTaxi1ServiceClient() {

        try {
            URL wsdlLocation = new URL("http://localhost:8080/test/Taxi1ServiceService?wsdl");
            QName serviceName = new QName("http://www.jboss.org/as/quickstarts/compensationsApi/travel/taxi1",
                    "Taxi1ServiceService");
            QName portName = new QName("http://www.jboss.org/as/quickstarts/compensationsApi/travel/taxi1",
                    "Taxi1Service");

            Service service = Service.create(wsdlLocation, serviceName);
            return service.getPort(portName, Taxi1Service.class);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error creating Web Service client", e);
        }
    }

    private Taxi2Service createTaxi2ServiceClient() {

        try {
            URL wsdlLocation = new URL("http://localhost:8080/test/Taxi2ServiceService?wsdl");
            QName serviceName = new QName("http://www.jboss.org/as/quickstarts/compensationsApi/travel/taxi2",
                    "Taxi2ServiceService");
            QName portName = new QName("http://www.jboss.org/as/quickstarts/compensationsApi/travel/taxi2",
                    "Taxi2Service");

            Service service = Service.create(wsdlLocation, serviceName);
            return service.getPort(portName, Taxi2Service.class);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error creating Web Service client", e);
        }
    }

}
