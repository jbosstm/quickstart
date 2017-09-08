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
package io.narayana.rts.lra.demo.tripcontroller.participant;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.narayana.lra.client.LRAClient;
import io.narayana.lra.client.LRAClientAPI;
import io.narayana.rts.lra.demo.model.Booking;
import io.narayana.rts.lra.demo.tripcontroller.service.TripService;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Optional;

import static io.narayana.lra.client.LRAClient.LRA_HTTP_HEADER;


/**
 * The quickstart scenario is:
 *
 * start LRA 1
 *   Book tripcontroller
 *   start LRA 2
 *     start LRA 3
 *       Book flight option 1
 *     start LRA 4
 *       Book flight option 2
 */
@RequestScoped
@Path(TripController.TRIP_PATH)
public class TripController {
    public static final String TRIP_PATH = "/trip";
    public static final String HOTEL_PATH = "/hotel";
    public static final String HOTEL_NAME_PARAM = "hotelName";
    public static final String HOTEL_BEDS_PARAM = "beds";
    public static final String FLIGHT_PATH = "/flight";
    public static final String FLIGHT_NUMBER_PARAM = "flightNumber";
    public static final String FLIGHT_BOOKINGID_PARAM = "bookingId";
    public static final String ALT_FLIGHT_NUMBER_PARAM = "altFlightNumber";
    public static final String FLIGHT_SEATS_PARAM = "flightSeats";

    private Client hotelClient;
    private Client flightClient;

    private WebTarget hotelTarget;
    private WebTarget flightTarget;

    @Inject
    private LRAClientAPI lraClient;

    @Inject
    private TripService tripService;
    @Context
    private UriInfo context;

    @Context
    private HttpServletRequest httpRequest;

    @PostConstruct
    private void initController() {
        try {
            URL HOTEL_SERVICE_BASE_URL = new URL("http://" + System.getProperty("hotel.service.http.host", "localhost") + ":" + Integer.getInteger("hotel.service.http.port", 8082));
            URL FLIGHT_SERVICE_BASE_URL = new URL("http://" + System.getProperty("flight.service.http.host", "localhost") + ":" + Integer.getInteger("flight.service.http.port", 8083));

            hotelClient = ClientBuilder.newClient();
            flightClient = ClientBuilder.newClient();

            hotelTarget = hotelClient.target(URI.create(new URL(HOTEL_SERVICE_BASE_URL, HOTEL_PATH).toExternalForm()));
            flightTarget = flightClient.target(URI.create(new URL(FLIGHT_SERVICE_BASE_URL, FLIGHT_PATH).toExternalForm()));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    @PreDestroy
    private void finiController() {
        hotelClient.close();
        flightClient.close();
    }

    @POST
    @Path("/book")
    @Produces(MediaType.APPLICATION_JSON)
    // delayClose because we want the LRA to be associated with a booking until the user confirms the booking
    public Response bookTrip( @HeaderParam(LRA_HTTP_HEADER) String lraId,
                              @QueryParam(HOTEL_NAME_PARAM) @DefaultValue("") String hotelName,
                              @QueryParam(HOTEL_BEDS_PARAM) @DefaultValue("1") Integer hotelGuests,
                              @QueryParam(FLIGHT_NUMBER_PARAM) @DefaultValue("") String flightNumber,
                              @QueryParam(ALT_FLIGHT_NUMBER_PARAM) @DefaultValue("") String altFlightNumber,
                              @QueryParam(FLIGHT_SEATS_PARAM) @DefaultValue("1") Integer flightSeats,
                              @QueryParam("mstimeout") @DefaultValue("0") Long timeout) throws BookingException {
        URL transaction = lraClient.startLRA("userTransaction", timeout);

        Booking hotelBooking = bookHotel(hotelName, hotelGuests, transaction);
        Booking flightBooking1 = bookFlight(flightNumber, flightSeats, transaction);
        Booking flightBooking2 = bookFlight(altFlightNumber, flightSeats, transaction);

        Booking tripBooking = new Booking(transaction.toString(), "Trip", hotelBooking, flightBooking1, flightBooking2);

        tripService.recordProvisionalBooking(tripBooking, transaction);

        return Response.status(Response.Status.CREATED).entity(tripBooking).build();
    }

    @POST
    @Path("/confirm")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response confirmTrip(@QueryParam("bookingId") String bookingId) throws NotFoundException, URISyntaxException, IOException {
        Booking tripBooking = getBooking(bookingId);
        if (tripBooking.getStatus() == Booking.BookingStatus.CANCEL_REQUESTED)
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Trying to setConfirmed a tripBooking which needs to be cancelled")
                    .build());

        // THIS WOULD LIKELY BE IN A SEPARATE BUSINESS METHOD
        // requestCancel the first flight found (and use the second one)
        Optional<Booking> firstFlight = Arrays.stream(tripBooking.getDetails()).filter(b -> "Flight".equals(b.getType())).findFirst();
        firstFlight.ifPresent(Booking::requestCancel);
        // check the booking to see if the client wants to requestCancel any dependent bookings
        Arrays.stream(tripBooking.getDetails()).filter(Booking::isCancelPending).forEach(b -> {
            WebTarget webTarget = flightTarget
                    .path("cancel")
                    .queryParam(FLIGHT_BOOKINGID_PARAM, b.getId());

            Response response = webTarget.request().post(Entity.text(""));
            if (response.getStatus() != Response.Status.OK.getStatusCode())
                throw new BookingException(response.getStatus(), "flight cancel problem");

            b.setCanceled();
        });

        tripService.confirmBooking(tripBooking);
        return Response.ok(tripBooking.toJson()).build();
    }

    @POST
    @Path("/cancel")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response cancelTrip(@QueryParam("bookingId") String bookingId) throws NotFoundException, URISyntaxException, IOException {
        Booking tripBooking = getBooking(bookingId);

        if (tripBooking.getStatus() != Booking.BookingStatus.CANCEL_REQUESTED && tripBooking.getStatus() != Booking.BookingStatus.PROVISIONAL)
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity("To late to requestCancel booking").build());

        tripService.cancelBooking(tripBooking);
        return Response.ok(tripBooking.toJson()).build();
    }

    @GET
    @Path("/{bookingId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Booking getBooking(@PathParam("bookingId") String bookingId) {
        return tripService.get(bookingId);
    }

    private Booking bookHotel(String name, int beds, URL transaction) throws BookingException {
        if (name == null || name.length() == 0 || beds <= 0)
            return null;

        WebTarget webTarget = hotelTarget
                .path("book")
                .queryParam(HOTEL_NAME_PARAM, name).queryParam(HOTEL_BEDS_PARAM, beds);

        Response response = webTarget.request().header(LRAClient.LRA_HTTP_HEADER, transaction.toString()).post(Entity.text(""));

        if (response.getStatus() != Response.Status.OK.getStatusCode())
            throw new BookingException(response.getStatus(), "hotel booking problem");

        return response.readEntity(Booking.class);
    }

    private Booking bookFlight(String flightNumber, int seats, URL transaction) throws BookingException {
        if (flightNumber == null || flightNumber.length() == 0 || seats <= 0)
            return null;

        WebTarget webTarget = flightTarget
                .path("book")
                .queryParam(FLIGHT_NUMBER_PARAM, flightNumber)
                .queryParam(FLIGHT_SEATS_PARAM, seats);

        Response response = webTarget.request().header(LRAClient.LRA_HTTP_HEADER, transaction.toString()).post(Entity.text(""));

        if (response.getStatus() != Response.Status.OK.getStatusCode())
            throw new BookingException(response.getStatus(), "flight booking problem");

        return response.readEntity(Booking.class);
    }
}

