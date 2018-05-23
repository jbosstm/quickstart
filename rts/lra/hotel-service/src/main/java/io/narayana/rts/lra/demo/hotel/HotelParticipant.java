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
package io.narayana.rts.lra.demo.hotel;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.narayana.rts.lra.demo.model.Booking;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.lra.annotation.Compensate;
import org.eclipse.microprofile.lra.annotation.Complete;
import org.eclipse.microprofile.lra.annotation.LRA;

import java.util.Collection;

import static io.narayana.lra.client.NarayanaLRAClient.LRA_HTTP_HEADER;

@RequestScoped
@Path("/")
@LRA(LRA.Type.SUPPORTS)
public class HotelParticipant {
    @Inject
    private HotelService service;

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @LRA(LRA.Type.REQUIRED)
    public Booking bookRoom(@HeaderParam(LRA_HTTP_HEADER) String lraId,
                            @QueryParam("hotelName") @DefaultValue("Default") String hotelName) {
        return service.book(lraId, hotelName);
    }

    @PUT
    @Path("/complete")
    @Produces(MediaType.APPLICATION_JSON)
    @Complete
    public Response completeWork(@HeaderParam(LRA_HTTP_HEADER) String lraId) throws NotFoundException, JsonProcessingException {
        service.get(lraId).setStatus(Booking.BookingStatus.CONFIRMED);
        return Response.ok(service.get(lraId).toJson()).build();
    }

    @PUT
    @Path("/compensate")
    @Produces(MediaType.APPLICATION_JSON)
    @Compensate
    public Response compensateWork(@HeaderParam(LRA_HTTP_HEADER) String lraId) throws NotFoundException, JsonProcessingException {
        service.get(lraId).setStatus(Booking.BookingStatus.CANCELLED);
        return Response.ok(service.get(lraId).toJson()).build();
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
}
