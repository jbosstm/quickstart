package io.narayana.rts.lra;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.lra.annotation.Compensate;
import org.eclipse.microprofile.lra.annotation.Complete;
import org.eclipse.microprofile.lra.annotation.ws.rs.LRA;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.eclipse.microprofile.lra.annotation.ws.rs.LRA.LRA_HTTP_RECOVERY_HEADER;

@Path("/" + MigratableResource.SERVICE_NAME)
public class MigratableResource {
    static final String SERVICE_NAME = "migrate";
    private static final AtomicBoolean halt = new AtomicBoolean(false);
    private static final AtomicBoolean completed = new AtomicBoolean(false);

    @LRA(value = LRA.Type.REQUIRED)
    @PUT
    public void doInTransaction() {
        halt.set(true); // halt when compensate or complete are called
    }

    @GET
    @Path("/completed")
    public boolean hasCompleted() {
        return completed.get();
    }

    @PUT
    @Path("/compensate")
    @Compensate
    public Response compensate() {
        return Response.ok().build();
    }

    @PUT
    @Path("/complete")
    @Complete
    public Response complete(@HeaderParam(LRA_HTTP_RECOVERY_HEADER) String recoveryUrl) {
        if (halt.get()) {
            int port = 8082;
            String completionLink = String.format("http://localhost:%d/%s/complete", port, SERVICE_NAME);

            migrateCompletionEndpoint(recoveryUrl, completionLink);

            Runtime.getRuntime().halt(1);
        }

        completed.set(true);

        System.out.printf("completed%n");

        return Response.ok().build();
    }

    private void migrateCompletionEndpoint(String recoveryUrl, String newEndpointLink) {
        try (Client client = ClientBuilder.newClient()) {
            try (Response response = client.target(recoveryUrl).request().put(Entity.text(newEndpointLink))) {
                if (response.getStatus() != 200) {
                    System.out.printf("Unable to move completion endpoint: %d", response.getStatus());
                }
            }
        }
    }
}
