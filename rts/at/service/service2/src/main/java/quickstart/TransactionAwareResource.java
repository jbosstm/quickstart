/*
 * JBoss, Home of Professional Open Source
 * Copyright 2016, Red Hat, Inc., and others contributors as indicated
 * by the @authors tag. All rights reserved.
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
package quickstart;

import java.util.concurrent.atomic.AtomicInteger;

import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import org.jboss.narayana.rest.integration.api.ParticipantsManagerFactory;

/**
 * An example of how a REST resource can act as a participant in a REST Atomic transaction.
 * For a complete implementation of a participant please refer to the test suite, in particular the inner class:
 * org.jboss.jbossts.star.test.BaseTest$TransactionalResource which implements all the responsibilities of a participant
 *
 * The example sends a service request which is handled by the method someServiceRequest. The request includes the
 * URL for registering durable participants within the transaction. This naive implementation assumes every request
 * with a valid enlistment URL is a request a new unit of transactional work and enlists a new URL into the transaction.
 * Thus if a client makes two http requests to the method someServiceRequest then the participant is enlisted twice
 * into the transaction but with different completion URLs. This facilitates the demonstration of 2 phase commit
 * processing.
 */
@Path(TransactionAwareResource.PSEGMENT)
public class TransactionAwareResource {
    public static final String PSEGMENT = "service";
    public static final String APPLICATION_ID = TransactionAwareResource.class.getName();
    public static String FAIL_COMMIT; // set by the client to simulate a failure by halting the JVM

    private static AtomicInteger workId = new AtomicInteger(0);

    @GET
    public Response someServiceRequest(@Context UriInfo info, @QueryParam("enlistURL") @DefaultValue("")String enlistUrl) {
        if (enlistUrl == null || enlistUrl.length() == 0)
            return Response.ok("non transactional request").build();

        Work work = new Work(workId.incrementAndGet());
        // enlist this resource instance
        ParticipantsManagerFactory.getInstance().enlist(APPLICATION_ID, enlistUrl, work);

        return Response.ok(Integer.toString(work.getId())).build();
    }

    @GET
    @Path("commits")
    public Response getNumberOfCommits() {
        return Response.ok(Integer.toString(Work.commitCnt.intValue())).build();
    }

    @GET
    @Path("aborts")
    public Response getNumberOfAborts() {
        return Response.ok(Integer.toString(Work.abortCnt.intValue())).build();
    }
}
