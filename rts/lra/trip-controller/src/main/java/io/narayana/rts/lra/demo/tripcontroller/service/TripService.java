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
package io.narayana.rts.lra.demo.tripcontroller.service;

import io.narayana.lra.client.LRAClientAPI;
import io.narayana.rts.lra.demo.model.Booking;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@ApplicationScoped
public class TripService {
    @Inject
    private LRAClientAPI lraClient;

    private Map<String, Booking> bookings = new HashMap<>();
    private Map<Booking, URL> transactions = new HashMap<>();

    public Booking confirmBooking(Booking booking) throws URISyntaxException, IOException {
        System.out.printf("Confirming tripBooking id %s (%s) status: %s%n",
                booking.getId(), booking.getName(), booking.getStatus());

        URL url = transactions.get(booking);
        String response = lraClient.closeLRA(url);

        Booking prev = bookings.putIfAbsent(booking.getId(), booking);
        prev.setConfirmed();
        mergeBookingResponse(prev, response);
        prev.setStatus(Booking.BookingStatus.CONFIRMED);
        return prev;
    }

    public Booking cancelBooking(Booking booking) throws URISyntaxException, IOException {
        System.out.printf("Canceling booking id %s (%s) status: %s%n",
                booking.getId(), booking.getName(), booking.getStatus());

        String response = lraClient.cancelLRA(transactions.get(booking));

        Booking prev = bookings.putIfAbsent(booking.getId(), booking);
        prev.setCanceled();
        mergeBookingResponse(prev, response);
        prev.setStatus(Booking.BookingStatus.CANCELLED);
        return prev;
    }

    public void recordProvisionalBooking(Booking booking, URL transaction) {
        bookings.putIfAbsent(booking.getId(), booking);

        transactions.put(booking, transaction);
    }

    public Booking get(String bookingId) throws NotFoundException {
        if (!bookings.containsKey(bookingId))
            throw new NotFoundException(Response.status(404).entity("Invalid bookingId id: " + bookingId).build());

        return bookings.get(bookingId);
    }

    private void mergeBookingResponse(Booking tripBooking, String responseData) throws URISyntaxException, IOException {
        List<Booking> bookingDetails = Booking.getBookingArray(responseData);
        Map<String, Booking> bookings = bookingDetails.stream()
                .collect(Collectors.toMap(Booking::getId, Function.identity()));

        // update tripBooking with bookings returned in the data returned from the trip setConfirmed request
        Arrays.stream(tripBooking.getDetails()) // the array of bookings in this trip booking
                .filter(b -> bookings.containsKey(b.getId())) // pick out bookings for which we have updated data
                .forEach(b -> b.merge(bookings.get(b.getId()))); // merge in the changes (returned from the setConfirmed request)
    }
}
