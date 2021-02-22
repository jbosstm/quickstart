package io.narayana.rts.lra;

import org.eclipse.microprofile.lra.annotation.Compensate;
import org.eclipse.microprofile.lra.annotation.Complete;
import org.eclipse.microprofile.lra.annotation.ws.rs.LRA;
import org.eclipse.microprofile.lra.annotation.ParticipantStatus;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.eclipse.microprofile.lra.annotation.ws.rs.LRA.LRA_HTTP_CONTEXT_HEADER;
import static org.eclipse.microprofile.lra.annotation.ws.rs.LRA.LRA_HTTP_RECOVERY_HEADER;

/**
 * for testing {@link org.eclipse.microprofile.lra.annotation.ws.rs.LRA}
 */
@Path("/")
@ApplicationScoped
public class CdiBasedResource {

    @Inject
    private StateHolder stats;

    @LRA(LRA.Type.REQUIRED)
    @Path("/cdi")
    @PUT
    public void doInTransaction(@DefaultValue("") @QueryParam("fault") String fault,
                                  @HeaderParam(LRA_HTTP_RECOVERY_HEADER) String rcvId,
                                  @HeaderParam(LRA_HTTP_CONTEXT_HEADER) String lraId) {
        stats.setFault(fault);
        stats.injectFault(StateHolder.FaultTarget.CDI, StateHolder.FaultWhen.BEFORE);
        // do something interesting
    }

    @Path("/cdi")
    @GET
    public String getStats() {
        return stats.toString();
    }

    @PUT
    @Path("/complete")
    @Produces(MediaType.APPLICATION_JSON)
    @Complete
    public Response completeWork(@HeaderParam(LRA_HTTP_CONTEXT_HEADER) String lraId, String userData)
            throws NotFoundException {
        stats.update(StateHolder.FaultTarget.CDI, ParticipantStatus.Completed);
        stats.injectFault(StateHolder.FaultTarget.CDI, StateHolder.FaultWhen.DURING);

        return Response.ok().build();
    }

    @PUT
    @Path("/compensate")
    @Produces(MediaType.APPLICATION_JSON)
    @Compensate
    public Response compensateWork(@HeaderParam(LRA_HTTP_CONTEXT_HEADER) String lraId, String userData)
            throws NotFoundException {
        stats.update(StateHolder.FaultTarget.CDI, ParticipantStatus.Compensated);
        stats.injectFault(StateHolder.FaultTarget.CDI, StateHolder.FaultWhen.DURING);

        return Response.ok().build();
    }
}
