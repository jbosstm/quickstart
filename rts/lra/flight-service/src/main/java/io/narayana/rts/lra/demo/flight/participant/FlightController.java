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
package io.narayana.rts.lra.demo.flight.participant;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.narayana.lra.annotation.Compensate;
import io.narayana.lra.annotation.Complete;
import io.narayana.lra.annotation.LRA;
import io.narayana.lra.annotation.NestedLRA;
import io.narayana.rts.lra.demo.flight.service.FlightService;
import io.narayana.rts.lra.demo.model.Booking;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static io.narayana.lra.client.LRAClient.LRA_HTTP_HEADER;

@RequestScoped
@Path("/")
@LRA(LRA.Type.SUPPORTS)
public class FlightController {
    @Inject
    private FlightService flightService;

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @LRA(LRA.Type.MANDATORY)
    @NestedLRA
    public Booking bookFlight(@HeaderParam(LRA_HTTP_HEADER) String lraId,
                              @QueryParam("flightNumber") @DefaultValue("") String flightNumber) {
        return flightService.book(lraId, flightNumber);
    }

    @POST
    @Path("/complete")
    @Produces(MediaType.APPLICATION_JSON)
    @Complete
    public Response completeWork(@HeaderParam(LRA_HTTP_HEADER) String lraId) throws NotFoundException, JsonProcessingException {
        flightService.updateBookingStatus(lraId, Booking.BookingStatus.CONFIRMED);
        return Response.ok(flightService.get(lraId).toJson()).build();
    }

    @POST
    @Path("/compensate")
    @Produces(MediaType.APPLICATION_JSON)
    @Compensate
    public Response compensateWork(@HeaderParam(LRA_HTTP_HEADER) String lraId) throws NotFoundException, JsonProcessingException {
        flightService.updateBookingStatus(lraId, Booking.BookingStatus.CANCELLED);
        return Response.ok(flightService.get(lraId).toJson()).build();
    }

    @DELETE
    @Path("/{bookingId}")
    @Produces(MediaType.APPLICATION_JSON)
    @LRA(LRA.Type.NOT_SUPPORTED)
    public Booking cancelFlight(@PathParam("bookingId") String bookingId) {
        return flightService.cancel(bookingId);
    }
}
