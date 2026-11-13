/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.narayana.quickstart.lra.jwt.service;

import io.narayana.lra.PropagateToken;
import io.quarkus.security.Authenticated;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.lra.annotation.Compensate;
import org.eclipse.microprofile.lra.annotation.Complete;
import org.eclipse.microprofile.lra.annotation.ParticipantStatus;
import org.eclipse.microprofile.lra.annotation.ws.rs.LRA;
import org.jboss.logging.Logger;
import org.jboss.narayana.quickstart.lra.jwt.model.OrderRequest;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import static org.eclipse.microprofile.lra.annotation.ws.rs.LRA.LRA_HTTP_CONTEXT_HEADER;

/**
 * Inventory participant. Joins the caller's LRA ({@link LRA.Type#MANDATORY}) and reserves
 * stock, releasing it again from {@link Compensate @Compensate} if the saga is cancelled.
 */
@ApplicationScoped
@Path("/inventory")
public class InventoryService {

    private static final Logger LOG = Logger.getLogger(InventoryService.class);

    private final Map<String, List<OrderRequest>> reservations = new ConcurrentHashMap<>();

    @Inject
    JsonWebToken jwt;

    @POST
    @Path("/reserve")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Authenticated
    @LRA(value = LRA.Type.MANDATORY, end = false)
    @PropagateToken
    public Response reserve(OrderRequest order,
                            @HeaderParam(LRA_HTTP_CONTEXT_HEADER) URI lraId,
                            @HeaderParam(HttpHeaders.AUTHORIZATION) String authHeader) {

        LOG.infof("Reserving %d x %s for order %s in LRA %s (caller=%s, authHeader=%s)",
                order.getQuantity(), order.getItemId(), order.getOrderId(), lraId,
                jwt.getName(), authHeader != null ? "present" : "absent");

        reservations.computeIfAbsent(lraId.toString(), id -> new CopyOnWriteArrayList<>()).add(order);
        return Response.ok(Map.of("status", "INVENTORY_RESERVED", "itemId", order.getItemId())).build();
    }

    @PUT
    @Path("/complete")
    @Produces(MediaType.APPLICATION_JSON)
    @Complete
    public Response complete(@HeaderParam(LRA_HTTP_CONTEXT_HEADER) URI lraId,
                             @HeaderParam(HttpHeaders.AUTHORIZATION) String authHeader) {

        List<OrderRequest> orders = reservations.remove(lraId.toString());
        LOG.infof("Complete callback for LRA %s (caller=%s, authHeader=%s): committed %s",
                lraId, jwt.getName(), authHeader != null ? "present" : "absent",
                itemsOf(orders));

        return Response.ok(ParticipantStatus.Completed.name()).build();
    }

    @PUT
    @Path("/compensate")
    @Produces(MediaType.APPLICATION_JSON)
    @Compensate
    public Response compensate(@HeaderParam(LRA_HTTP_CONTEXT_HEADER) URI lraId,
                               @HeaderParam(HttpHeaders.AUTHORIZATION) String authHeader) {

        List<OrderRequest> orders = reservations.remove(lraId.toString());
        LOG.infof("Compensate callback for LRA %s (caller=%s, authHeader=%s): released %s",
                lraId, jwt.getName(), authHeader != null ? "present" : "absent",
                itemsOf(orders));

        return Response.ok(ParticipantStatus.Compensated.name()).build();
    }

    private static String itemsOf(List<OrderRequest> orders) {
        return orders == null || orders.isEmpty() ? "<unknown>"
                : orders.stream().map(OrderRequest::getItemId).collect(Collectors.joining(", "));
    }
}
