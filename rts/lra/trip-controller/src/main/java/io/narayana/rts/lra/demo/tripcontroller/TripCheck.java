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
package io.narayana.rts.lra.demo.tripcontroller;

import io.narayana.rts.lra.demo.model.Booking;

import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;
import java.util.Arrays;

/**
 * For testing - verify that the business data returned when ending an LRA is the same as that returned by directly
 * interrogating each sra.demo.service involved in the booking
 */
class TripCheck {
    private TripCheck() { // utility classes should not have an externally accessable constructors
    }

    static boolean validateBooking(Booking booking, boolean isConfirm, WebTarget hotelTarget, WebTarget flightTarget) throws BookingException {
        final BookingException[] bookingException = {null};
        Booking bookingCopy = new Booking(booking);
        final int[] confirmCount = {0};
        final int[] cancelCount = {0};

        // NB parallel() results in IllegalStateException: WFLYWELD0039 because
        // ... trying to access a weld deployment with a Thread Context ClassLoader that is not associated with the deployment
        Arrays.stream(booking.getDetails()).forEach(b -> {
            try {
                checkDependentBooking(b, hotelTarget, flightTarget);

                if (b.getStatus().equals(Booking.BookingStatus.CANCELLED)) {
                    cancelCount[0] += 1;
                } else if (b.getStatus().equals(Booking.BookingStatus.CONFIRMED)) {
                    confirmCount[0] += 1;
                }
            } catch (BookingException e) {
                bookingException[0] = e;
            }
        });

        if (isConfirm) {
            // the hotel and only one of the flight bookings should be confirmed
            if (confirmCount[0] != 2 || cancelCount[0] != 1) {
                System.out.printf("TripCheck: validateBooking: the hotel and only one of the flight bookings should have been confirmed%n");
                return false;
            }
        } else {
            if (cancelCount[0] != 3) {
                System.out.printf("TripCheck: validateBooking: the hotel and both flight bookings should have be cancelled%n");
                return false;
            }
        }

        return bookingCopy.equals(booking); // Note that this is a shallow comparison
    }

    private static void checkDependentBooking(Booking booking, WebTarget hotelTarget, WebTarget flightTarget) throws BookingException {
        if ("Hotel".equals(booking.getType()))
            checkDependentBooking(hotelTarget, booking);
        else if ("Flight".equals(booking.getType()))
            checkDependentBooking(flightTarget, booking);
    }

    private static void checkDependentBooking(WebTarget target, Booking booking) throws BookingException {
        Response response = null;

        try {
            response = target.path(booking.getEncodedId()).request().get(); // service must be listening on this path

            checkResponse(response, Response.Status.OK, "Could not lookup booking status");

            booking.merge(response.readEntity(Booking.class));
        } catch (Exception e) {
            System.out.printf("TripCheck: checkDependentBooking: %s: %s%n",
                    target.path("info").path(booking.getEncodedId()).getUri().toString(),
                    e.getMessage());

            if (response != null) {
                System.out.printf("TripCheck: checkDependentBooking: JAX-RS response was %d%n",
                        response.getStatus());
            }


            e.printStackTrace();
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    private static void checkResponse(Response response, Response.Status expect, String message) throws BookingException {
        if (response.getStatus() != expect.getStatusCode())
            throw new BookingException(response.getStatus(), message);
    }
}

