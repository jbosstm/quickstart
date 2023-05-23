package io.narayana.sra.demo.api;

import io.narayana.sra.demo.constant.ServiceConstant;
import io.narayana.sra.demo.model.Booking;
import io.narayana.sra.demo.service.BookingException;
import io.narayana.sra.demo.service.TripService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.jbossts.star.annotation.SRA;
import org.jboss.jbossts.star.annotation.Status;
import org.jboss.jbossts.star.client.SRAParticipant;
import org.jboss.jbossts.star.client.SRAStatus;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import static org.jboss.jbossts.star.client.SRAClient.RTS_HTTP_CONTEXT_HEADER;

@RequestScoped
@Path(ServiceConstant.TRIP_PATH)
@SRA(SRA.Type.SUPPORTS)
public class TripController extends SRAParticipant {

    private Client hotelClient;
    private Client flightClient;

    private WebTarget hotelTarget;
    private WebTarget flightTarget;

    @Inject
    TripService tripService;

    @PostConstruct
    void initController() {
        try {
            int hotelServicePort = Integer.getInteger(ServiceConstant.HOTEL_SERVICE_PORT_PROPERTY, 8083);
            int flightServicePort = Integer.getInteger(ServiceConstant.FLIGHT_SERVICE_PORT_PROPERTY, 8084);

            URL HOTEL_SERVICE_BASE_URL = new URL("http://localhost:" + hotelServicePort);
            URL FLIGHT_SERVICE_BASE_URL = new URL("http://localhost:" + flightServicePort);

            hotelClient = ClientBuilder.newClient();
            flightClient = ClientBuilder.newClient();

            hotelTarget = hotelClient.target(URI.create(new URL(HOTEL_SERVICE_BASE_URL, ServiceConstant.HOTEL_PATH).toExternalForm()));
            flightTarget = flightClient.target(URI.create(new URL(FLIGHT_SERVICE_BASE_URL, ServiceConstant.FLIGHT_PATH).toExternalForm()));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    @PreDestroy
    void finiController() {
        hotelClient.close();
        flightClient.close();
    }

    /**
     * The quickstart scenario is:
     *
     * start LRA 1
     *   Book hotel
     *   start LRA 2
     *     start LRA 3
     *       Book flight option 1
     *     start LRA 4
     *       Book flight option 2
     *
     * @param hotelName hotel name
     * @param hotelGuests number of beds required
     * @param flightSeats number of people flying
     */
    @POST
    @Path("/book")
    @Produces(MediaType.APPLICATION_JSON)
    // The default value of the end attribute is true so the transaction is committed after booking the trip.
    @SRA(SRA.Type.REQUIRED)
    public Response bookTrip( @HeaderParam(RTS_HTTP_CONTEXT_HEADER) String sraId,
                              @QueryParam(ServiceConstant.HOTEL_NAME_PARAM) @DefaultValue("") String hotelName,
                              @QueryParam(ServiceConstant.HOTEL_BEDS_PARAM) @DefaultValue("1") Integer hotelGuests,
                              @QueryParam(ServiceConstant.FLIGHT_NUMBER_PARAM) @DefaultValue("456") String flightNumber,
                              @QueryParam(ServiceConstant.ALT_FLIGHT_NUMBER_PARAM) @DefaultValue("123") String altFlightNumber,
                              @QueryParam(ServiceConstant.FLIGHT_SEATS_PARAM) @DefaultValue("1") Integer flightSeats,
                              @QueryParam("mstimeout") @DefaultValue("500") Long timeout) throws BookingException {

        Booking hotelBooking = bookHotel(sraId, hotelName, hotelGuests);
        Booking flightBooking = bookFlight(sraId, flightNumber, flightSeats);

        Booking tripBooking = new Booking(sraId, "Trip", hotelBooking, flightBooking);

        return Response.status(Response.Status.CREATED).entity(tripBooking).build();
    }

    @GET
    @Path("/status")
    @Produces(MediaType.APPLICATION_JSON)
    @Status
    @SRA(SRA.Type.NOT_SUPPORTED)
    public Response status(@HeaderParam(RTS_HTTP_CONTEXT_HEADER) String sraId) throws NotFoundException {
        Booking booking = tripService.get(sraId);

        return Response.ok(booking.getStatus().name()).build(); // TODO convert to a CompensatorStatus if we we're enlisted in an SRA
    }

    private Booking bookHotel(String sraId, String name, int beds) throws BookingException {
        if (name == null || name.length() == 0 || beds <= 0)
            return null;

        WebTarget webTarget = hotelTarget
                .path("book")
                .queryParam(ServiceConstant.HOTEL_NAME_PARAM, name)
                .queryParam(ServiceConstant.HOTEL_BEDS_PARAM, beds);

        Response response = webTarget.request().header(RTS_HTTP_CONTEXT_HEADER, sraId).post(Entity.text(""));

        if (response.getStatus() != Response.Status.OK.getStatusCode())
            throw new BookingException(response.getStatus(), "Hotel booking problem: " + response.getStatus());


        return response.readEntity(Booking.class);
    }

    private Booking bookFlight(String sraId, String flightNumber, int seats) throws BookingException {
        if (flightNumber == null || flightNumber.length() == 0 || seats <= 0)
            return null;

        WebTarget webTarget = flightTarget
                .path("book")
                .queryParam(ServiceConstant.FLIGHT_NUMBER_PARAM, flightNumber)
                .queryParam(ServiceConstant.FLIGHT_SEATS_PARAM, seats);

        Response response = webTarget.request().header(RTS_HTTP_CONTEXT_HEADER, sraId).post(Entity.text(""));

        if (response.getStatus() != Response.Status.OK.getStatusCode())
            throw new BookingException(response.getStatus(), "flight booking problem: " + response.getStatus());

        return response.readEntity(Booking.class);
    }

    @Override
    protected SRAStatus updateParticipantState(SRAStatus status, String activityId) {
        System.out.printf("SRA: %s: Updating trip participant state to: %s%n", activityId, status);
        return status;
    }
}

