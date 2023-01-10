/*
 *
 * Copyright The Narayana Authors
 *
 * SPDX-License-Identifier: LGPL-2.1-only
 *
 */

package org.jboss.narayana.quickstarts.mongodb.simple.resources;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.narayana.compensations.api.TransactionCompensatedException;
import org.jboss.narayana.quickstarts.mongodb.simple.BankingService;

@Path(BankingServiceJaxRs.ROOT_PATH)
@ApplicationScoped
public class BankingServiceJaxRs {

    @Inject
    BankingService bankingService;

    public static final String ROOT_PATH = "banking";

    public static final String TRANSFER_MONEY = "transferMoney";

    @PUT
    @Path(BankingServiceJaxRs.TRANSFER_MONEY)
    @Consumes(MediaType.TEXT_PLAIN)
    public Response transferMoney(
            @QueryParam("fromAccount") String fromAccount,
            @QueryParam("toAccount") String toAccount,
            @QueryParam("amount") double amount) {
        try {
            bankingService.transferMoney(fromAccount, toAccount, amount);
            return Response.ok().build();
        } catch (TransactionCompensatedException ex) {
            return Response.serverError().build();
        }
    }
}
