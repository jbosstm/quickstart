package io.narayana.rts.lra.demo.tripcontroller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.narayana.lra.client.internal.NarayanaLRAClient;
import io.narayana.rts.lra.demo.model.Booking;

import jakarta.ws.rs.client.WebTarget;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static jakarta.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;

@ApplicationScoped
public class TripService {
    @Inject
    private NarayanaLRAClient lraClient;

    private Map<String, Booking> bookings = new HashMap<>();

    public void confirmBooking(Booking booking, WebTarget hotelTarget, WebTarget flightTarget) throws URISyntaxException, IOException {
        System.out.printf("Confirming tripBooking id %s (%s) status: %s%n",
                booking.getId(), booking.getName(), booking.getStatus());

        lraClient.closeLRA(new URI(booking.getId()));

        if (!TripCheck.validateBooking(booking, true, hotelTarget, flightTarget))
            throw new BookingException(INTERNAL_SERVER_ERROR.getStatusCode(), "LRA response data does not match booking data");

//        mergeBookingResponse(booking, response);
        booking.setStatus(Booking.BookingStatus.CONFIRMED);
    }

    public void cancelBooking(Booking booking, WebTarget hotelTarget, WebTarget flightTarget) throws URISyntaxException, IOException {
        System.out.printf("Cancelling booking id %s (%s) status: %s%n",
                booking.getId(), booking.getName(), booking.getStatus());

        lraClient.cancelLRA(new URI(booking.getId()));

        if (!TripCheck.validateBooking(booking, false, hotelTarget, flightTarget))
            throw new BookingException(INTERNAL_SERVER_ERROR.getStatusCode(), "LRA response data does not match booking data");

//        mergeBookingResponse(booking, response);
        booking.setStatus(Booking.BookingStatus.CANCELLED);
    }

    public void recordProvisionalBooking(Booking booking) throws MalformedURLException {
        bookings.putIfAbsent(booking.getId(), booking);
    }

    public Booking get(String bookingId) throws NotFoundException {
        if (!bookings.containsKey(bookingId))
            throw new NotFoundException(Response.status(404).entity("Invalid bookingId id: " + bookingId).build());

        return bookings.get(bookingId);
    }

    private void mergeBookingResponse(Booking tripBooking, String responseData) throws URISyntaxException, IOException {
        responseData = responseData.replaceAll("\"", "");
        responseData = responseData.replaceAll("\\\\", "\"");
        List<Booking> bookingDetails = Arrays.asList(new ObjectMapper().readValue(responseData, Booking[].class));

        Map<String, Booking> bookings = bookingDetails.stream()
                .collect(Collectors.toMap(Booking::getId, Function.identity()));

        // update tripBooking with bookings returned in the data returned from the trip setConfirmed request
        Arrays.stream(tripBooking.getDetails()) // the array of bookings in this trip booking
                .filter(b -> bookings.containsKey(b.getId())) // pick out bookings for which we have updated data
                .forEach(b -> b.merge(bookings.get(b.getId()))); // merge in the changes (returned from the setConfirmed request)
    }

    public Collection<Booking> getAll() {
        return bookings.values();
    }
}