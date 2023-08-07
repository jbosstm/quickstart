package io.narayana.sra.demo.service;

import io.narayana.sra.demo.model.Booking;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.jboss.jbossts.star.client.SRAClient;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@ApplicationScoped
public class TripService extends BookingStore{
    private SRAClient lraClient;

    public TripService() throws MalformedURLException, URISyntaxException {
        this.lraClient = new SRAClient();
    }

    public Booking confirmBooking(Booking tripBooking) {
        System.out.printf("Confirming tripBooking id %s (%s) status: %s%n",
                tripBooking.getId(), tripBooking.getName(), tripBooking.getStatus());

        if (tripBooking.getStatus() == Booking.BookingStatus.CANCEL_REQUESTED)
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Trying to setConfirmed a tripBooking which needs to be cancelled")
                    .build());

        Booking prev = add(tripBooking);

        if (prev != null)
            System.out.printf("Seen this tripBooking before%n");

        // check the booking to see if the client wants to requestCancel any dependent bookings
        Arrays.stream(tripBooking.getDetails()).filter(Booking::isCancelPending).forEach(b -> {
            lraClient.cancelSRA(SRAClient.sraToURL(b.getSraId(), "Invalid " + b.getType() + " tripBooking id format"));
            b.setCanceled();
        });

        tripBooking.setConfirming();

        Arrays.stream(tripBooking.getDetails()).forEach(booking -> booking.setStatus(Booking.BookingStatus.CONFIRMED));

//        lraClient.commitSRA(SRAClient.sraToURL(tripBooking.getSraId()));

        tripBooking.setConfirmed();

        return mergeBookingResponse(tripBooking);
    }

    public Booking cancelBooking(Booking bookings) {
        System.out.printf("Canceling booking id %s (%s) status: %s%n",
                bookings.getId(), bookings.getName(), bookings.getStatus());

        if (bookings.getStatus() != Booking.BookingStatus.CANCEL_REQUESTED && bookings.getStatus() != Booking.BookingStatus.PROVISIONAL)
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity("To late to requestCancel booking").build());

        Booking prev = add(bookings);

        if (prev != null)
            System.out.printf("Seen this booking before%n");

        bookings.requestCancel();

        Arrays.stream(bookings.getDetails()).forEach(booking -> booking.setStatus(Booking.BookingStatus.CANCELLED));

    //    lraClient.cancelSRA(SRAClient.sraToURL(booking.getSraId(), "Invalid trip booking id format"));

        bookings.setCanceled();

        return mergeBookingResponse(bookings);
    }

    private Booking mergeBookingResponse(Booking tripBooking) {
        URL bookingId = SRAClient.sraToURL(tripBooking.getSraId());
        List<String> bookingDetails = lraClient.getResponseData(bookingId); // each string is a json encoded tripBooking

//        List<Booking> bookings = bookingDetails.stream().map(Booking::fromJson).collect(Collectors.toList());

        // convert the list of bookings into a map keyed by Booking::getId()
        Map<String, Booking> bookings = bookingDetails.stream()
                .map(Booking::fromJson)
                .collect(Collectors.toMap(Booking::getId, Function.identity()));

        // update tripBooking with bookings returned in the data returned from the trip setConfirmed request
        Arrays.stream(tripBooking.getDetails()) // the array of bookings in this trip booking
                .filter(b -> bookings.containsKey(b.getId())) // pick out bookings for which we have updated data
                .forEach(b -> b.merge(bookings.get(b.getId()))); // merge in the changes (returned from the setConfirmed request)

        return tripBooking;
    }
    public Booking book(String id, String type, Booking... bookings) {
        Booking booking = new Booking(id, type, bookings);

        add(booking);

        return booking;
    }
}
