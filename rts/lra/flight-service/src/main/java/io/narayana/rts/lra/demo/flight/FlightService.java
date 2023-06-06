package io.narayana.rts.lra.demo.flight;

import io.narayana.lra.client.NarayanaLRAClient;
import io.narayana.rts.lra.demo.model.Booking;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class FlightService {

    @Inject
    private NarayanaLRAClient lraClient;

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

    public Booking cancel(String bookingId) throws URISyntaxException {
        Booking booking = get(bookingId);
        booking.setStatus(Booking.BookingStatus.CANCEL_REQUESTED);
        lraClient.cancelLRA(new URI(bookingId));
        return booking;
    }

    public Collection<Booking> getAll() {
        return bookings.values();
    }
}