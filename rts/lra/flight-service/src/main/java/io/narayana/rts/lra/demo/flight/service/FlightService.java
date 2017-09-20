/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2017, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package io.narayana.rts.lra.demo.flight.service;

import io.narayana.lra.client.LRAClient;
import io.narayana.lra.client.LRAClientAPI;
import io.narayana.rts.lra.demo.model.Booking;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class FlightService {

    @Inject
    private LRAClientAPI lraClient;

    private Map<String, Booking> bookings = new HashMap<>();

    public Booking book(String bid, String flightNumber) {
        Booking booking = new Booking(bid, flightNumber, "Flight");
        Booking earlierBooking = bookings.putIfAbsent(booking.getId(), booking);
        return earlierBooking == null ? booking : earlierBooking;
    }

    public Booking get(String bookingId) throws NotFoundException {
        if (!bookings.containsKey(bookingId))
            throw new NotFoundException(Response.status(404).entity("Invalid bookingId id: " + bookingId).build());

        return bookings.get(bookingId);
    }

    public void updateBookingStatus(String bookingId, Booking.BookingStatus status) {
        get(bookingId).setStatus(status);
    }

    public Booking cancel(String bookingId) {
        Booking booking = get(bookingId);
        booking.setStatus(Booking.BookingStatus.CANCEL_REQUESTED);
        lraClient.cancelLRA(LRAClient.lraToURL(bookingId));
        return booking;
    }
}
