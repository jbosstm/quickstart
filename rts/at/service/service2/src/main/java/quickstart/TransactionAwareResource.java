package quickstart;

import java.util.concurrent.atomic.AtomicInteger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

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

    private static AtomicInteger workId = new AtomicInteger(0);

    @GET
    public Response someServiceRequest(@Context UriInfo info, @QueryParam("enlistURL") String enlistUrl) {
        if (enlistUrl == null || enlistUrl.length() == 0)
            return Response.ok("non transactional request").build();

        Work work = new Work(workId.incrementAndGet());
        ParticipantsManagerFactory.getInstance().enlist(this.getClass().getName(), enlistUrl, work);

        return Response.ok(Integer.toString(work.getId())).build();
    }

    @GET
    @Path("query")
    public Response someServiceRequest() {
        return Response.ok(Integer.toString(Work.commitCnt.intValue())).build();
    }
}
