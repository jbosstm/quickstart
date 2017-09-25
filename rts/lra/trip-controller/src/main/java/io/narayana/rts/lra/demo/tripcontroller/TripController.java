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

import com.fasterxml.jackson.core.JsonProcessingException;
import io.narayana.lra.annotation.LRA;
import io.narayana.lra.client.LRAClient;
import io.narayana.lra.client.LRAClientAPI;
import io.narayana.rts.lra.demo.model.Booking;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
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
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;


/**
 * The quickstart scenario is:
 * <p>
 * start LRA 1
 * Book tripcontroller
 * start LRA 2
 * start LRA 3
 * Book flight option 1
 * start LRA 4
 * Book flight option 2
 */
@RequestScoped
@Path("/")
public class TripController {
    private Client hotelClient;
    private Client flightClient;

    private WebTarget hotelTarget;
    private WebTarget flightTarget;

    @Inject
    private LRAClientAPI lraClient;

    @Inject
    private TripService service;

    @PostConstruct
    private void initController() throws MalformedURLException {
        hotelClient = ClientBuilder.newClient();
        flightClient = ClientBuilder.newClient();
        hotelTarget = hotelClient.target(new URL("http://" + System.getProperty("hotel.service.http.host", "localhost") + ":" + Integer.getInteger("hotel.service.http.port", 8082)).toExternalForm());
        flightTarget = flightClient.target(new URL("http://" + System.getProperty("flight.service.http.host", "localhost") + ":" + Integer.getInteger("flight.service.http.port", 8083)).toExternalForm());
    }

    @PreDestroy
    private void finiController() {
        hotelClient.close();
        flightClient.close();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    // delayClose because we want the LRA to be associated with a booking until the user confirms the booking
    @LRA(delayClose = true, join = false)
    public Response bookTrip(@QueryParam("hotelName") @DefaultValue("TheGrand") String hotelName,
                             @QueryParam("flightNumber1") @DefaultValue("firstClass") String flightNumber,
                             @QueryParam("flightNumber2") @DefaultValue("secondClass") String altFlightNumber,
                             @Context UriInfo uriInfo) throws BookingException, MalformedURLException, UnsupportedEncodingException {
        String lraId = lraClient.getCurrent().toString();
        Booking hotelBooking = bookHotel(hotelName, lraId);
        Booking flightBooking1 = bookFlight(flightNumber, lraId);
        Booking flightBooking2 = bookFlight(altFlightNumber, lraId);

        Booking tripBooking = new Booking(lraId, "Trip", hotelBooking, flightBooking1, flightBooking2);

        service.recordProvisionalBooking(tripBooking);

        UriBuilder builder = uriInfo.getAbsolutePathBuilder();
        builder.path(URLEncoder.encode(tripBooking.getId(), "UTF-8"));
        return Response.created(builder.build()).entity(tripBooking).build();
    }

    @PUT
    @Path("/{bookingId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response confirmTrip(@PathParam("bookingId") String bookingId) throws NotFoundException, URISyntaxException, IOException {
        Booking tripBooking = service.get(bookingId);
        if (tripBooking.getStatus() == Booking.BookingStatus.CANCEL_REQUESTED)
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Trying to setConfirmed a tripBooking which needs to be cancelled")
                    .build());

        // THIS WOULD LIKELY BE IN A SEPARATE BUSINESS METHOD - requestCancel the first flight found (and use the second one)
        Optional<Booking> firstFlight = Arrays.stream(tripBooking.getDetails()).filter(b -> "Flight".equals(b.getType())).findFirst();
        firstFlight.ifPresent(Booking::requestCancel);
        Arrays.stream(tripBooking.getDetails()).filter(Booking::isCancelPending).forEach(b -> {
            WebTarget webTarget = null;
            try {
                webTarget = flightTarget.path(URLEncoder.encode(b.getId().toString(), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                throw new BookingException(-1, "flight cancel problem: UnsupportedEncodingException" + e);
            }

            Response response = webTarget.request().delete();
            if (response.getStatus() != Response.Status.OK.getStatusCode())
                throw new BookingException(response.getStatus(), "flight cancel problem: " + b.getId());

            b.setStatus(Booking.BookingStatus.CANCELLED);
        });

        service.confirmBooking(tripBooking);
        return Response.ok(tripBooking.toJson()).build();
    }

    @DELETE
    @Path("/{bookingId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response cancelTrip(@PathParam("bookingId") String bookingId) throws NotFoundException, URISyntaxException, IOException {
        Booking tripBooking = service.get(bookingId);
        if (tripBooking.getStatus() != Booking.BookingStatus.CANCEL_REQUESTED && tripBooking.getStatus() != Booking.BookingStatus.PROVISIONAL)
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity("Too late to requestCancel booking").build());
        service.cancelBooking(tripBooking);
        return Response.ok(tripBooking.toJson()).build();
    }

    @GET
    @Path("/{bookingId}")
    @Produces(MediaType.APPLICATION_JSON)
    @LRA(LRA.Type.NOT_SUPPORTED)
    public Booking getBooking(@PathParam("bookingId") String bookingId) throws JsonProcessingException {
        return service.get(bookingId);
    }


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<Booking> getAll() {
        return service.getAll();
    }

    private Booking bookHotel(String name, String bookingId) throws BookingException {
        WebTarget webTarget = hotelTarget.path("/")
                .queryParam("hotelName", name);
        Response response = webTarget.request().header(LRAClient.LRA_HTTP_HEADER, bookingId).post(Entity.text(""));
        if (response.getStatus() != Response.Status.OK.getStatusCode())
            throw new BookingException(response.getStatus(), "hotel booking problem");
        return response.readEntity(Booking.class);
    }

    private Booking bookFlight(String flightNumber, String bookingId) throws BookingException {
        WebTarget webTarget = flightTarget.path("/")
                .queryParam("flightNumber", flightNumber);
        Response response = webTarget.request().header(LRAClient.LRA_HTTP_HEADER, bookingId).post(Entity.text(""));
        if (response.getStatus() != Response.Status.OK.getStatusCode())
            throw new BookingException(response.getStatus(), "flight booking problem");
        return response.readEntity(Booking.class);
    }
}

