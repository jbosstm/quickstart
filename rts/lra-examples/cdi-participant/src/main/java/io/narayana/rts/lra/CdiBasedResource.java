package io.narayana.rts.lra;

import org.eclipse.microprofile.lra.annotation.Compensate;
import org.eclipse.microprofile.lra.annotation.Complete;
import org.eclipse.microprofile.lra.annotation.ws.rs.LRA;
import org.eclipse.microprofile.lra.annotation.ParticipantStatus;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

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
