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
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.lra.annotation.AfterLRA;
import org.eclipse.microprofile.lra.annotation.LRAStatus;
import org.eclipse.microprofile.lra.annotation.ws.rs.LRA;
import org.jboss.logging.Logger;
import org.jboss.narayana.quickstart.lra.jwt.model.OrderRequest;

import java.net.URI;
import java.util.Map;

import static org.eclipse.microprofile.lra.annotation.ws.rs.LRA.LRA_HTTP_CONTEXT_HEADER;
import static org.eclipse.microprofile.lra.annotation.ws.rs.LRA.LRA_HTTP_ENDED_CONTEXT_HEADER;

/**
 * Orchestrates an order as a saga.
 *
 * <p>{@code createOrder} opens an LRA ({@link LRA.Type#REQUIRED}) and, thanks to
 * {@link PropagateToken @PropagateToken}, the inbound Bearer token is captured and attached
 * to the LRA-protocol traffic this service sends to the coordinator (start/close). The calls
 * to the inventory and payment participants forward the same token explicitly, so every hop
 * carries the caller identity.</p>
 */
@ApplicationScoped
@Path("/orders")
public class OrderService {

    private static final Logger LOG = Logger.getLogger(OrderService.class);

    @Inject
    JsonWebToken jwt;

    // Trusted destination for the participant calls; never derived from the inbound request's Host.
    @ConfigProperty(name = "participant.base-url", defaultValue = "http://localhost:8081")
    String participantBaseUrl;

    @POST
    @Path("/create")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Authenticated
    @LRA(LRA.Type.REQUIRED)
    @PropagateToken
    public Response createOrder(OrderRequest order,
                                @HeaderParam(LRA_HTTP_CONTEXT_HEADER) URI lraId,
                                @HeaderParam(HttpHeaders.AUTHORIZATION) String authHeader) {

        LOG.infof("Creating order %s in LRA %s (caller=%s)", order.getOrderId(), lraId, jwt.getName());

        URI inventoryUri = UriBuilder.fromUri(participantBaseUrl).path("inventory").path("reserve").build();
        URI paymentUri = UriBuilder.fromUri(participantBaseUrl).path("payment").path("process").build();

        try (Client client = ClientBuilder.newClient()) {
            call(client, inventoryUri, order, lraId, authHeader, "inventory reservation");
            call(client, paymentUri, order, lraId, authHeader, "payment");
        }

        LOG.infof("Order %s created successfully", order.getOrderId());
        return Response.ok(Map.of("orderId", order.getOrderId(), "status", "CONFIRMED")).build();
    }

    @PUT
    @Path("/after")
    @AfterLRA
    public Response afterLRA(@HeaderParam(LRA_HTTP_ENDED_CONTEXT_HEADER) URI lraId, LRAStatus status) {
        LOG.infof("AfterLRA for order LRA %s: final status %s", lraId, status);
        return Response.ok().build();
    }

    private void call(Client client, URI target, OrderRequest order, URI lraId, String authHeader, String what) {
        try (Response response = client.target(target)
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, authHeader)
                .header(LRA_HTTP_CONTEXT_HEADER, lraId)
                .post(Entity.json(order))) {

            if (response.getStatus() != Response.Status.OK.getStatusCode()) {
                // Throwing here fails the JAX-RS method, which cancels the LRA and drives
                // @Compensate on every participant that had already joined.
                throw new WebApplicationException(what + " failed with HTTP " + response.getStatus(),
                        Response.Status.BAD_GATEWAY);
            }
        }
    }
}
