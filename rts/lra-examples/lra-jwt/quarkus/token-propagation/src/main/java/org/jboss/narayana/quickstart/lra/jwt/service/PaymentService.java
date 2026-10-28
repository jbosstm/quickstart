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
 * Payment participant. Joins the caller's LRA ({@link LRA.Type#MANDATORY}) and reserves
 * funds, refunding them from {@link Compensate @Compensate} if the saga is cancelled.
 *
 * <p>A negative {@code amount} makes the payment decline (HTTP 402), which lets the example
 * demonstrate compensation: the orchestrator cancels the LRA and the inventory reservation
 * that already joined is released.</p>
 */
@ApplicationScoped
@Path("/payment")
public class PaymentService {

    private static final Logger LOG = Logger.getLogger(PaymentService.class);

    private final Map<String, List<OrderRequest>> payments = new ConcurrentHashMap<>();

    @Inject
    JsonWebToken jwt;

    @POST
    @Path("/process")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Authenticated
    @LRA(value = LRA.Type.MANDATORY, end = false)
    @PropagateToken
    public Response process(OrderRequest order,
                            @HeaderParam(LRA_HTTP_CONTEXT_HEADER) URI lraId,
                            @HeaderParam(HttpHeaders.AUTHORIZATION) String authHeader) {

        LOG.infof("Processing payment of %.2f for order %s in LRA %s (caller=%s, authHeader=%s)",
                order.getAmount(), order.getOrderId(), lraId, jwt.getName(),
                authHeader != null ? "present" : "absent");

        if (order.getAmount() < 0) {
            LOG.warnf("Payment declined for order %s (negative amount)", order.getOrderId());
            return Response.status(Response.Status.PAYMENT_REQUIRED)
                    .entity(Map.of("status", "PAYMENT_DECLINED")).build();
        }

        payments.computeIfAbsent(lraId.toString(), id -> new CopyOnWriteArrayList<>()).add(order);
        return Response.ok(Map.of("status", "PAYMENT_RESERVED", "amount", order.getAmount())).build();
    }

    @PUT
    @Path("/complete")
    @Produces(MediaType.APPLICATION_JSON)
    @Complete
    public Response complete(@HeaderParam(LRA_HTTP_CONTEXT_HEADER) URI lraId,
                             @HeaderParam(HttpHeaders.AUTHORIZATION) String authHeader) {

        List<OrderRequest> orders = payments.remove(lraId.toString());
        LOG.infof("Complete callback for LRA %s (caller=%s, authHeader=%s): captured %s",
                lraId, jwt.getName(), authHeader != null ? "present" : "absent",
                amountsOf(orders));

        return Response.ok(ParticipantStatus.Completed.name()).build();
    }

    @PUT
    @Path("/compensate")
    @Produces(MediaType.APPLICATION_JSON)
    @Compensate
    public Response compensate(@HeaderParam(LRA_HTTP_CONTEXT_HEADER) URI lraId,
                               @HeaderParam(HttpHeaders.AUTHORIZATION) String authHeader) {

        List<OrderRequest> orders = payments.remove(lraId.toString());
        LOG.infof("Compensate callback for LRA %s (caller=%s, authHeader=%s): refunded %s",
                lraId, jwt.getName(), authHeader != null ? "present" : "absent",
                amountsOf(orders));

        return Response.ok(ParticipantStatus.Compensated.name()).build();
    }

    private static String amountsOf(List<OrderRequest> orders) {
        return orders == null || orders.isEmpty() ? "<unknown>"
                : orders.stream().map(o -> String.valueOf(o.getAmount())).collect(Collectors.joining(", "));
    }
}
