package org.jboss.as.quickstarts.wsat.jtabridge.tojta;

import com.arjuna.mw.wst11.UserTransaction;
import com.arjuna.mw.wst11.UserTransactionFactory;

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

/**
 * Simple set of tests for the RestaurantServiceAT
 *
 * @author paul.robinson@redhat.com, 2012-01-04
 */
@RunWith(Arquillian.class)
public class BridgeToJTATest {

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
        ut = UserTransactionFactory.userTransaction();
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
        System.out.println("[CLIENT] Beginning the first WS-AT transaction");
        ut.begin();
        System.out.println("[CLIENT] Calling makeBooking on the WS client stub.");
        client.makeBooking(1);
        System.out.println("[CLIENT] Booking successful, about to commit the WS-AT transaction.");
        ut.commit();

        System.out.println("[CLIENT] Beginning the second WS-AT transaction");
        ut.begin();
        System.out.println("[CLIENT] Calling getBookingCount on the WS client stub.");
        int counter = client.getBookingCount();
        System.out.println("[CLIENT] Booking count obtained successfully, about to commit the WS-AT transaction.");
        ut.commit();

        System.out.println("[CLIENT] Asserting that the booking was successful");
        Assert.assertEquals(1, counter);
    }

    @Test
    public void testClientDrivenRollback() throws Exception {
        System.out.println("[CLIENT] Beginning the first WS-AT transaction");
        ut.begin();
        System.out.println("[CLIENT] Calling makeBooking on the WS client stub.");
        client.makeBooking(1);
        System.out.println("[CLIENT] Booking successful, about to rollback the WS-AT transaction");
        ut.rollback();

        System.out.println("[CLIENT] Beginning the second WS-AT transaction");
        ut.begin();
        System.out.println("[CLIENT] Calling getBookingCount on the WS client stub.");
        int counter = client.getBookingCount();
        System.out.println("[CLIENT] Booking count obtained successfully, about to commit the WS-AT transaction.");
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
