package io.narayana.rts.lra;

import org.eclipse.microprofile.lra.annotation.LRA;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import static org.eclipse.microprofile.lra.client.LRAClient.LRA_HTTP_HEADER;
import static org.eclipse.microprofile.lra.client.LRAClient.LRA_HTTP_RECOVERY_HEADER;

/**
 * for testing {@link org.eclipse.microprofile.lra.annotation.LRA}
 * in combination with {@link org.eclipse.microprofile.lra.participant.LRAManagement}
 */
@Path("/")
@ApplicationScoped
public class MixedResource {

    @Inject
    private StateHolder stats;

    @Context
    private UriInfo context;

    private static Client msClient;
    private WebTarget msTarget;

    @PostConstruct
    private void postConstruct() {
        int servicePort = Integer.getInteger("swarm.http.port", 8080);

        try {
            URL microserviceBaseUrl = new URL("http://localhost:" + servicePort);

            // setting up the client
            msClient = ClientBuilder.newClient();

            msTarget = msClient.target(URI.create(new URL(microserviceBaseUrl, "/").toExternalForm()));
        } catch (MalformedURLException e) {
            System.err.printf("WARN: unabled to construct URL: %s%n", e.getMessage());
        }
    }

    @LRA(LRA.Type.REQUIRED)
    @Path("/mixed")
    @PUT
    public void doInTransaction(@DefaultValue("") @QueryParam("fault") String fault,
                                  @HeaderParam(LRA_HTTP_RECOVERY_HEADER) String rcvId,
                                  @HeaderParam(LRA_HTTP_HEADER) String lraId) throws MalformedURLException {
        URL lra = new URL(lraId);

        doWork(lra, "/cdi", fault);
        doWork(lra, "/api", fault);
    }

    @Path("/mixed")
    @GET
    public String getStats() {
        return stats.toString();
    }

    private void doWork(URL lraId, String path, String fault) {
        stats.setFault(fault);

        try (Response response = msTarget.path(path)
                .queryParam("fault", fault)
                .request().put(Entity.text(""))) {
            if (!response.getStatusInfo().getFamily().equals(Response.Status.Family.SUCCESSFUL)) {
                throw new WebApplicationException(response);
            }
        }
    }
}
