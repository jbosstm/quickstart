package org.jboss.narayana.quickstarts.restat;

import junit.framework.Assert;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.narayana.quickstarts.restat.restaurant.RestaurantServiceAT;
import org.jboss.narayana.quickstarts.restat.taxi.TaxiServiceAT;
import org.jboss.narayana.txframework.impl.handlers.restat.client.UserTransaction;
import org.jboss.narayana.txframework.impl.handlers.restat.client.UserTransactionFactory;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.client.ProxyFactory;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ByteArrayAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @Author paul.robinson@redhat.com 19/05/2012
 */
@RunWith(Arquillian.class)
public class IndirectTXManagementTest {

    private static final int SERVICE_PORT = 8080;
    private static final String TAXI_URL = "http://localhost:" + SERVICE_PORT + "/test";
    private static final String RESTAURANT_URL = "http://localhost:" + SERVICE_PORT + "/test";
    private UserTransaction ut;
    private RestaurantServiceAT restaurantClient;
    private TaxiServiceAT taxiClient;

    @Deployment
    public static WebArchive createTestArchive() {
        WebArchive archive = ShrinkWrap.create(WebArchive.class, "test.war")
                .addPackages(true, "org.jboss.narayana.quickstarts.restat")
                .addAsWebInfResource(new ByteArrayAsset("<interceptors><class>org.jboss.narayana.txframework.impl.handlers.restat.client.RestTXRequiredInterceptor</class></interceptors>".getBytes()),
                        ArchivePaths.create("beans.xml"))
                .addAsWebInfResource("web.xml");
        archive.delete(ArchivePaths.create("META-INF/MANIFEST.MF"));

        String ManifestMF = "Manifest-Version: 1.0\n"
                + "Dependencies: org.jboss.resteasy.resteasy-jaxrs,javax.ws.rs.api,javax.ejb.api,org.jboss.jts,org.jboss.narayana.txframework\n";
        archive.setManifest(new StringAsset(ManifestMF));

        return archive;

    }

    @Before
    public void setupTest() throws Exception {
        ut = UserTransactionFactory.userTransaction();
        restaurantClient = ProxyFactory.create(RestaurantServiceAT.class, RESTAURANT_URL);
        taxiClient = ProxyFactory.create(TaxiServiceAT.class, TAXI_URL);
    }

    @After
    public void teardownTest() throws Exception {
        restaurantClient.reset();
        taxiClient.reset();
        rollbackIfActive(ut);
    }

    @Test
    public void clientDrivenCommitTest() throws Exception {
        ut.begin();

        ClientResponse response1 = (ClientResponse) restaurantClient.makeBooking();
        response1.releaseConnection();

        ClientResponse response2 = (ClientResponse) taxiClient.makeBooking();
        response2.releaseConnection();

        ut.commit();

        ClientResponse<Integer> countResponse2 = (ClientResponse<Integer>) taxiClient.getBookingCount();
        Assert.assertEquals(new Integer(1), countResponse2.getEntity(Integer.class));
        countResponse2.releaseConnection();

        ClientResponse<Integer> countResponse1 = (ClientResponse<Integer>) restaurantClient.getBookingCount();
        Assert.assertEquals(new Integer(1), countResponse1.getEntity(Integer.class));
        countResponse1.releaseConnection();

    }

    @Test
    public void clientDrivenRollbackTest() throws Exception {
        ut.begin();

        ClientResponse response1 = (ClientResponse) restaurantClient.makeBooking();
        response1.releaseConnection();

        ClientResponse response2 = (ClientResponse) taxiClient.makeBooking();
        response2.releaseConnection();

        ut.rollback();

        ClientResponse<Integer> countResponse2 = (ClientResponse<Integer>) taxiClient.getBookingCount();
        Assert.assertEquals(new Integer(0), countResponse2.getEntity(Integer.class));
        countResponse2.releaseConnection();

        ClientResponse<Integer> countResponse1 = (ClientResponse<Integer>) restaurantClient.getBookingCount();
        Assert.assertEquals(new Integer(0), countResponse1.getEntity(Integer.class));
        countResponse1.releaseConnection();

    }

    private void rollbackIfActive(UserTransaction ut) {
        try {
            ut.rollback();
        } catch (Throwable th2) {
            // do nothing, not active
        }
    }
}
