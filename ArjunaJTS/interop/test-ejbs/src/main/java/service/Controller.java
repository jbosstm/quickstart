/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2017, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
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
