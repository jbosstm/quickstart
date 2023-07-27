package io.narayana.sra.demo.api;


import io.narayana.sra.demo.constant.ServiceConstant;
import io.narayana.sra.demo.model.Booking;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import org.jboss.jbossts.star.annotation.SRA;
import org.jboss.jbossts.star.annotation.Status;
import org.jboss.jbossts.star.client.SRAParticipant;

import io.narayana.sra.demo.service.HotelService;
import org.jboss.jbossts.star.client.SRAStatus;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import static org.jboss.jbossts.star.client.SRAClient.RTS_HTTP_CONTEXT_HEADER;

@RequestScoped
@Path(ServiceConstant.HOTEL_PATH)
@SRA(SRA.Type.SUPPORTS)
public class HotelController extends SRAParticipant {

    @Inject
    HotelService hotelService;

    @POST
    @Path("/book")
    @Produces(MediaType.APPLICATION_JSON)
    // end = false because we want the SRA to be associated with a booking until the user confirms the booking
    @SRA(value = SRA.Type.REQUIRED, end = false)
    public Booking bookRoom(@HeaderParam(RTS_HTTP_CONTEXT_HEADER) String sraId,
                         @QueryParam(ServiceConstant.HOTEL_NAME_PARAM) @DefaultValue("Default") String hotelName,
                         @QueryParam(ServiceConstant.HOTEL_BEDS_PARAM) @DefaultValue("1") Integer beds,
                         @QueryParam("mstimeout") @DefaultValue("500") Long timeout) {
        return hotelService.book(sraId, hotelName, beds);
    }
    @GET
    @Path("/status")
    @Produces(MediaType.APPLICATION_JSON)
    @Status
    @SRA(SRA.Type.NOT_SUPPORTED)
    public Response status(@HeaderParam(RTS_HTTP_CONTEXT_HEADER) String sraId) throws NotFoundException {
        Booking booking = hotelService.get(sraId);

        return Response.ok(booking.getStatus().name()).build(); // TODO convert to a CompensatorStatus if we we're enlisted in an SRA
    }
    @GET
    @Path("/info/{bookingId}")
    @Produces(MediaType.APPLICATION_JSON)
    @SRA(SRA.Type.SUPPORTS)
    public Booking getBooking(@PathParam("bookingId") String bookingId) {
        return hotelService.get(bookingId);
    }

    @Override
    protected SRAStatus updateParticipantState(SRAStatus status, String bookingId) {
        System.out.printf("SRA: %s: Updating hotel participant state to: %s%n", bookingId, status);
        switch (status) {
            case TransactionCommitted:
                hotelService.updateBookingStatus(bookingId, Booking.BookingStatus.CONFIRMED);
                return status;
            case TransactionRolledBack:
                hotelService.updateBookingStatus(bookingId, Booking.BookingStatus.CANCELLED);
                return status;
            default:
                return status;
        }
    }
}
