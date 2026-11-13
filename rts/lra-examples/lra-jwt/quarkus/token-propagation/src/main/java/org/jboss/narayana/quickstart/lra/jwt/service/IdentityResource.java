/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.narayana.quickstart.lra.jwt.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.narayana.quickstart.lra.jwt.util.JwtKeyGenerator;

/**
 * Stands in for an external identity provider.
 *
 * <p>{@code GET /auth/token} mints a demo token and {@code GET /auth/jwks} publishes the
 * verification key. Both endpoints are intentionally unauthenticated so a client can
 * bootstrap a token and so the MicroProfile JWT runtime can fetch the JWKS referenced by
 * {@code mp.jwt.verify.publickey.location}.</p>
 */
@ApplicationScoped
@Path("/auth")
public class IdentityResource {

    @GET
    @Path("/token")
    @Produces(MediaType.TEXT_PLAIN)
    public String token(@DefaultValue("demo-user") @QueryParam("subject") String subject) {
        return "Bearer " + JwtKeyGenerator.createToken(subject, "user");
    }

    @GET
    @Path("/jwks")
    @Produces(MediaType.APPLICATION_JSON)
    public Response jwks() {
        return Response.ok(JwtKeyGenerator.publicJwks()).build();
    }
}
