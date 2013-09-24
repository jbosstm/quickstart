/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
 * See the copyright.txt in the distribution for a full listing
 * of individual contributors.
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
 *
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
package org.jboss.narayana.quickstarts.compensationsApi.taxi2.jaxws;

import org.jboss.narayana.quickstarts.compensationsApi.taxi2.BookingStatus;

import javax.jws.WebMethod;
import javax.jws.WebService;
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
