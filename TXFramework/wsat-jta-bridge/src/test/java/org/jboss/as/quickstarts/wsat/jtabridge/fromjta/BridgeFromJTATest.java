package org.jboss.as.quickstarts.wsat.jtabridge.fromjta;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.as.quickstarts.wsat.jtabridge.RestaurantServiceATImpl;
import org.jboss.as.quickstarts.wsat.jtabridge.jaxws.RestaurantServiceAT;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ByteArrayAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.transaction.UserTransaction;

/**
 * Simple set of tests for the RestaurantServiceAT
 *
 * @author paul.robinson@redhat.com, 2012-01-04
 */
@RunWith(Arquillian.class)
public class BridgeFromJTATest {

    private static final String ManifestMF = "Manifest-Version: 1.0\n"
            + "Dependencies: org.jboss.narayana.txframework\n";

    private UserTransaction ut;
    private RestaurantServiceAT client;

    /**
     * Create the deployment archive to be deployed by Arquillian.
     *
     * @return a JavaArchive representing the required deployment
     */
    @Deployment
    public static JavaArchive createTestArchive() {

        //todo: Does the application developer have to specify the interceptor?
        JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "bridge.jar")
                .addPackages(true, RestaurantServiceATImpl.class.getPackage())
                .addAsManifestResource("persistence.xml")
                .addAsManifestResource(new ByteArrayAsset("<interceptors><class>org.jboss.narayana.txframework.impl.ServiceRequestInterceptor</class></interceptors>".getBytes()),
                        ArchivePaths.create("beans.xml"));

        archive.delete(ArchivePaths.create("META-INF/MANIFEST.MF"));

        archive.setManifest(new StringAsset(ManifestMF));

        return archive;
    }

    @Before
    public void setupTest() throws Exception {
        Context initialContext = new InitialContext();
        ut = (UserTransaction)initialContext.lookup("java:comp/UserTransaction");
        client = ATBridgeClient.newInstance();
    }

    @After
    public void teardownTest() throws Exception {
        rollbackIfActive(ut);
        try {
            ut.begin();
            client.resetBookingCount();
            ut.commit();
        } finally {
            rollbackIfActive(ut);
        }
    }

    @Test
    public void testCommit() throws Exception {
        System.out.println("[CLIENT] Beginning the first JTA transaction");
        ut.begin();
        System.out.println("[CLIENT] Calling makeBooking on the WS client stub. The registered interceptor will bridge rom JTA to WS-AT");
        client.makeBooking(1);
        System.out.println("[CLIENT] Booking successful, about to commit the JTA transaction. This will also cause the bridged WS-AT transaction to commit");
        ut.commit();

        System.out.println("[CLIENT] Beginning the second JTA transaction");
        ut.begin();
        System.out.println("[CLIENT] Calling getBookingCount on the WS client stub. The registered interceptor will bridge rom JTA to WS-AT");
        int counter = client.getBookingCount();
        System.out.println("[CLIENT] Booking count obtained successfully, about to commit the JTA transaction. This will also cause the bridged WS-AT transaction to commit");
        ut.commit();

        System.out.println("[CLIENT] Asserting that the booking was successful");
        Assert.assertEquals(1, counter);
    }

    @Test
    public void testClientDrivenRollback() throws Exception {
        System.out.println("[CLIENT] Beginning the first JTA transaction");
        ut.begin();
        System.out.println("[CLIENT] Calling makeBooking on the WS client stub. The registered interceptor will bridge rom JTA to WS-AT");
        client.makeBooking(1);
        System.out.println("[CLIENT] Booking successful, about to rollback the JTA transaction. This will also cause the bridged WS-AT transaction to rollback");
        ut.rollback();

        System.out.println("[CLIENT] Beginning the second JTA transaction");
        ut.begin();
        System.out.println("[CLIENT] Calling getBookingCount on the WS client stub. The registered interceptor will bridge rom JTA to WS-AT");
        int counter = client.getBookingCount();
        System.out.println("[CLIENT] Booking count obtained successfully, about to commit the JTA transaction. This will also cause the bridged WS-AT transaction to commit");
        ut.commit();

        System.out.println("[CLIENT] Asserting that the booking was *not* successful");
        Assert.assertEquals(0, counter);
    }

    /**
     * Utility method for rolling back a transaction if it is currently active.
     *
     * @param ut The User Business Activity to cancel.
     */
    private void rollbackIfActive(UserTransaction ut) {
        try {
            ut.rollback();
        } catch (Throwable th2) {
            // do nothing, not active
        }
    }
}
