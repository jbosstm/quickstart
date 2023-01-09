/*
 * Copyright 2015, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */
package org.jboss.narayana.rts;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

import org.jboss.jbossts.star.provider.HttpResponseException;
import org.jboss.jbossts.star.util.TxLinkNames;
import org.jboss.jbossts.star.util.TxMediaType;
import org.jboss.jbossts.star.util.TxStatus;
import org.jboss.jbossts.star.util.TxSupport;
import org.jboss.logging.Logger;

/**
 * An example of how a REST resource can act as a participant in a REST Atomic transaction.
 * For a complete implementation of a participant please refer to the test suite, in particular the inner class:
 * org.jboss.jbossts.star.test.BaseTest$TransactionalResource which implements all the responsibilities of a participant
 *
 * The example sends a service request which is handled by the method someServiceRequest. The request includes the
 * URL for registering durable participants within the transaction. This naive implementation assumes every POST request
 * with a valid enlistment URL is a request a new unit of transactional work and enlists a new URL into the transaction.
 * Thus if a client makes two http requests to the method someServiceRequest then the participant is enlisted twice
 * into the transaction but with different completion URLs. This facilitates the demonstration of 2 phase commit
 * processing.
 */
@Path(TransactionAwareResource.PSEGMENT)
public class TransactionAwareResource {
    protected final static Logger log = Logger.getLogger(TxnTest.class);
    public static final String PSEGMENT = "service";
    private static AtomicInteger workId = new AtomicInteger(0);
    private static Map<String, String> pendingWork = new HashMap<String, String>();

    private static Map<String, String> values = new HashMap<String, String>();

    @ApplicationPath("/")
    public static class ServiceApp extends Application
    {
        @Override
        public Set<Class<?>> getClasses()
        {
            HashSet<Class<?>> classes = new HashSet<Class<?>>();
            classes.add(TransactionAwareResource.class);

            return classes;
        }
    }

    @POST
    @Produces( "text/plain" )
    public Response someServiceRequest(@Context UriInfo info, @QueryParam("enlistURL") String enlistUrl, @DefaultValue("") String content) {
        if (enlistUrl == null || enlistUrl.length() == 0) {
            // None-transactional test method
            values.put(info.getBaseUri().toString(), content);
            return Response.ok(content).build();
        }


        String wid = Integer.toString(workId.incrementAndGet());

        // Will be using the wid in the terminator link headers as per spec requirement
        String recoveryCoordinator = enlist(enlistUrl, wid, info);
        // The recoveryCoordinator is ignored in this demo but it would normally be made durable during prepare

        try {
            // enlist using linkHeader
            pendingWork.put(wid, content);

            return Response.ok(wid).build();
        } catch (HttpResponseException e){
            return Response.status(e.getActualResponse()).build();
        } catch (Throwable e) {
            return Response.serverError().build();
        }
    }

    @GET
    @Path("query")
    public Response someServiceRequest(@Context UriInfo info) {
        return Response.ok(values.get(info.getBaseUri().toString())).build();
    }

    /*
     * this method handles PUT requests to the url that the participant gave to the REST Atomic Transactions
     * implementation (in the someServiceRequest method). This is the endpoint that the transaction manager interacts
     * with when it needs participants to prepare/commit/rollback their transactional work.
     */
    @PUT
    @Path("{wId}/terminator")
    public Response terminate(@Context UriInfo info, @PathParam("wId") @DefaultValue("")String wId, String content) {
        TxStatus status = TxSupport.toTxStatus(content);

        System.out.printf("Service ep %s: PUT request to terminate url: wId=%s, status:=%s%n",
                info.getAbsolutePath(), wId, content);

        if (status.isCommit() || status.isCommitOnePhase()) {
            String newValue = pendingWork.remove(wId);
            values.put(info.getBaseUri().toString(), newValue == null ? "" : newValue);
        } else if (!status.isPrepare() && !status.isAbort()) {
            return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).build();
        } else if (status.isAbort()) {
            pendingWork.remove(wId);
        } else if (status.isPrepare()) {
            // Typically you would make the pendingWork for wId durable at this point
            // You would also make the recovery coordinator from enlist durable at this point
        }

        log.tracef("terminated workId %s enlist%n", wId );

        return Response.ok(TxSupport.toStatusContent(status.name())).build();
    }

    /*
     * Report the terminator URL used by this participant
     */
    @HEAD
    @Path("{pId}/participant")
    public Response getTerminator(@Context UriInfo info, @PathParam("pId") @DefaultValue("")String wId) {
        String linkHeader = makeTwoPhaseAwareParticipantLinkHeader(info, false, wId);

        return Response.ok().header("Link", linkHeader).build();
    }

    private String enlist(String enlistUrl, String wid, UriInfo info) {
        String linkHeader = makeTwoPhaseAwareParticipantLinkHeader(info, false, String.valueOf(wid));
        Map<String, String> reqHeaders = new HashMap<String, String>();

        reqHeaders.put("Link", linkHeader);

        return new TxSupport().httpRequest(new int[] {HttpURLConnection.HTTP_CREATED}, enlistUrl, "POST",
                TxMediaType.POST_MEDIA_TYPE, null, null, reqHeaders);
    }

    private String makeTwoPhaseAwareParticipantLinkHeader(
            UriInfo info, boolean vParticipant, String wId) {
        String baseURI = info.getBaseUri() + info.getPath().substring(1); // avoid '//' in urls

        StringBuilder resourcePrefix = new StringBuilder(baseURI);

        if (wId != null)
            resourcePrefix.append('/').append(wId);

        resourcePrefix.append('/');

        StringBuilder participantLinkHeader = new StringBuilder(
                Link.fromUri(resourcePrefix + TxLinkNames.PARTICIPANT_RESOURCE).rel(TxLinkNames.PARTICIPANT_RESOURCE).build().toString()).
                        append(',').
                append(Link.fromUri(resourcePrefix + TxLinkNames.PARTICIPANT_TERMINATOR).rel(TxLinkNames.PARTICIPANT_TERMINATOR).build());

        if (vParticipant)
            participantLinkHeader.append(',').
                    append(Link.fromUri(resourcePrefix + TxLinkNames.VOLATILE_PARTICIPANT).rel(TxLinkNames.VOLATILE_PARTICIPANT).build());


        return participantLinkHeader.toString();
    }
}

