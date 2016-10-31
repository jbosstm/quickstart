/*
 * JBoss, Home of Professional Open Source
 * Copyright 2016, Red Hat, Inc., and others contributors as indicated
 * by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */
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

    @Compensatable(distributed = true)
    public TravelBookingResult makeBooking(String name, Date date) {

        Integer hotelBookingID = hotelBookingClient.makeBooking(name, date);

        Integer taxi1BookingID = null;
        Integer taxi2BookingID = null;

        try {
            taxi1BookingID = taxi1BookingClient.makeBooking(name, date);
        } catch (SOAPFaultException e) {
            taxi2BookingID = taxi2BookingClient.makeBooking(name, date);
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
