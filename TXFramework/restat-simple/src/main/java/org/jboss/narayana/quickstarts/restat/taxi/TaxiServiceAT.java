package org.jboss.narayana.quickstarts.restat.taxi;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

/**
 * @Author paul.robinson@redhat.com 22/05/2012
 */
@Path("/taxi")
public interface TaxiServiceAT {

    @POST
    @Produces("text/plain")
    //Bug in TXFramework prevents @Path on this method (JBTM-1191)
    public Response makeBooking();
    
    @GET
    @Produces("text/plain")
    @Path("getBookingCount")
    public Response getBookingCount();

    @GET
    @Produces("text/plain")
    @Path("reset")
    public Response reset();
}
