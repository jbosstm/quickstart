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
package io.narayana.rts.lra.demo.hotel.participant;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.narayana.lra.annotation.Compensate;
import io.narayana.lra.annotation.CompensatorStatus;
import io.narayana.lra.annotation.Complete;
import io.narayana.lra.annotation.LRA;
import io.narayana.lra.annotation.Leave;
import io.narayana.lra.annotation.Status;
import io.narayana.lra.client.InvalidLRAId;
import io.narayana.lra.client.LRAClient;
import io.narayana.rts.lra.demo.hotel.service.HotelService;
import io.narayana.rts.lra.demo.model.Booking;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.HashMap;
import java.util.Map;

import static io.narayana.lra.client.LRAClient.LRA_HTTP_HEADER;

@RequestScoped
@Path(HotelController.HOTEL_PATH)
@LRA(LRA.Type.SUPPORTS)
public class HotelController {
    public static final String HOTEL_PATH = "/hotel";
    public static final String HOTEL_NAME_PARAM = "hotelName";
    public static final String HOTEL_BEDS_PARAM = "beds";

    @Context
    private UriInfo context;

    @Context
    private HttpServletRequest httpRequest;

    private Map<String, CompensatorStatus> compensatorStatusMap = new HashMap<>();

    @Inject
    private HotelService hotelService;

    @POST
    @Path("/book")
    @Produces(MediaType.APPLICATION_JSON)
    @LRA(LRA.Type.REQUIRED)
    public Booking bookRoom(@QueryParam(HOTEL_NAME_PARAM) @DefaultValue("Default") String hotelName,
                            @QueryParam(HOTEL_BEDS_PARAM) @DefaultValue("1") Integer beds,
                            @QueryParam("mstimeout") @DefaultValue("500") Long timeout) {

        return hotelService.book(getCurrentActivityId(), hotelName, beds);
    }

    @POST
    @Path("/complete")
    @Produces(MediaType.APPLICATION_JSON)
    @Complete
    public Response completeWork() throws NotFoundException {
        return updateState(CompensatorStatus.Completed, getCurrentActivityId());
    }

    @POST
    @Path("/compensate")
    @Produces(MediaType.APPLICATION_JSON)
    @Compensate
    public Response compensateWork() throws NotFoundException {
        return updateState(CompensatorStatus.Compensated, getCurrentActivityId());
    }

    @GET
    @Path("/info/{bookingId}")
    @Produces(MediaType.APPLICATION_JSON)
    @LRA(LRA.Type.SUPPORTS)
    public Booking getBooking(@PathParam("bookingId") String bookingId) {
        return hotelService.get(bookingId);
    }

    @GET
    @Path("/status")
    @Produces(MediaType.APPLICATION_JSON)
    @Status
    @LRA(LRA.Type.NOT_SUPPORTED)
    public Response status() throws NotFoundException {
        String lraId = getCurrentActivityId();

        if (lraId == null)
            throw new InvalidLRAId("null", "not present on CompletionHandler#status request", null);

        if (!compensatorStatusMap.containsKey(lraId))
            throw new InvalidLRAId(lraId, "CompletionHandler#status request: unknown lra id", null);

        // return status ok together with optional completion data or one of the other codes with a url that
        // returns

        /*
         * the compensator will either return a 200 OK code (together with optional completion data) or a URL which
         * indicates the outcome. That URL can be probed (via GET) and will simply return the same (implicit) information:
         *
         * <URL>/cannot-compensate
         * <URL>/cannot-complete
         *
         * TODO I am returning the status url instead. And if the status is compensated or completed then performing
         * GET on it will return 200 OK together with a compensator specific string that the business operation can
         * reason about, otherwise some other suitable status code is returned together with one of he valid
         * compensator states.
         */
        return updateState(compensatorStatusMap.get(lraId), lraId);
    }

    @PUT
    @Path("/leave")
    @Produces(MediaType.APPLICATION_JSON)
    @Leave
    public Response leaveWork(@HeaderParam(LRA_HTTP_HEADER) String lraId) throws NotFoundException {
        return Response.ok().build();
    }

    /**
     * Get the LRA context of the currently running method.
     * Note that @HeaderParam(LRA_HTTP_HEADER) does not match the header (done't know why) so we the httpRequest
     *
     * @return the LRA context of the currently running method
     */
    private String getCurrentActivityId() {
        return httpRequest.getHeader(LRA_HTTP_HEADER);
    }

    /**
     * If the compensator was successful return a 200 status code and optionally an application specific string
     * that can be used by whoever closed the LRA (that triggered this compensator).
     * <p>
     * Otherwise return a status url that can be probed to obtain the final outcome when it is ready
     *
     * @param status
     * @param activityId
     * @return
     */
    private Response updateState(CompensatorStatus status, String activityId) {
        CompensatorStatus newStatus;
        /*
         * Tell the compensator to move to the requested state.
         */
        switch (status) {
            case Completed:
                hotelService.updateBookingStatus(activityId, Booking.BookingStatus.CONFIRMED);
                newStatus = status;
                break;
            case Compensated:
                hotelService.updateBookingStatus(activityId, Booking.BookingStatus.CANCELLED);
                newStatus = status;
                break;
            default:
                newStatus = status;
        }

        compensatorStatusMap.put(activityId, newStatus); // NB in the demo we never remove completed activities

        switch (newStatus) {
            case Completed:
            case Compensated:
                String data = null;
                try {
                    data = hotelService.get(activityId).toJson();
                } catch (NotFoundException | JsonProcessingException e) {
                    System.out.printf("No booking for hotel id %s%n", activityId);
                }
                return data == null ? Response.ok().build() : Response.ok(data).build();
            default:
                String statusUrl = String.format("%s/%s/activity/status", context.getBaseUri(), LRAClient.getLRAId(activityId));
                return Response.status(Response.Status.ACCEPTED).entity(Entity.text(statusUrl)).build();
        }
    }
}
