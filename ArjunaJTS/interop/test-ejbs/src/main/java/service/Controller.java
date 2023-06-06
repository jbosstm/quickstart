package service;

import jakarta.inject.Inject;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;

@Path("/")
public class Controller {
	@Inject
	ControllerBean service;

	@GET
	@Path("/local/{arg}")
	public String getLocalNextCount(@DefaultValue("") @PathParam("arg") String arg) {
		return "Next: " + service.getNext(true, arg);
	}

	@GET
	@Path("/remote/{jndiPort}")
	public Response getRemoteNextCount(@DefaultValue("0") @PathParam("jndiPort") int jndiPort) {
		return getRemoteNextCountWithASAndError(jndiPort, null, null);
	}

	@GET
	@Path("/remote/{jndiPort}/{as}/{failureType}")
	public Response getRemoteNextCountWithASAndError(
			@DefaultValue("0") @PathParam("jndiPort") int jndiPort,
			@DefaultValue("") @PathParam("as") String as,
			@PathParam("failureType") String failureType) {
		return Response.status(200)
				.entity("Next: " + service.getNext(false, as, jndiPort, failureType))
				.build();
	}
}