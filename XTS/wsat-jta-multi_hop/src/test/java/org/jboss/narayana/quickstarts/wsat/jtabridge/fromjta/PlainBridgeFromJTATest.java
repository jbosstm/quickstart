package org.jboss.narayana.quickstarts.wsat.jtabridge.fromjta;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.narayana.quickstarts.wsat.jtabridge.first.FirstServiceATImpl;
import org.jboss.narayana.quickstarts.wsat.jtabridge.first.jaxws.FirstServiceAT;
import org.jboss.narayana.quickstarts.wsat.jtabridge.second.SecondServiceATImpl;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

/**
 * Simple set of tests for the FirstServiceAT
 *
 * @author paul.robinson@redhat.com, 2012-01-04
 */
// @RunWith(Arquillian.class)
public class PlainBridgeFromJTATest {

    private static final String ManifestMF = "Manifest-Version: 1.0\n"
            + "Dependencies: org.jboss.xts,org.jboss.jts\n";

    private FirstServiceAT firstClient;


    @Before
    public void setupTest() throws Exception {
        firstClient = FirstClient.newInstance();
    }

    @After
    public void teardownTest() throws Exception {
        System.out.println("[CLIENT] Don't reset counter!!!!!!");
        // firstClient.resetCounter();
    }

    @Deployment
    public static WebArchive createTestArchive() {

        WebArchive archive = ShrinkWrap.create(WebArchive.class, "bridge.war")
                .addPackages(true, FirstServiceATImpl.class.getPackage())
                .addPackages(true, SecondServiceATImpl.class.getPackage())
                .addPackages(true, FirstClient.class.getPackage())
                .addAsWebInfResource(new File("src/main/resources/META-INF/persistence.xml"), "classes/META-INF/persistence.xml")
                .addAsWebInfResource(new File("src/main/resources/context-handlers.xml"), "classes/context-handlers.xml");

        archive.setManifest(new StringAsset(ManifestMF));
        return archive;
    }

    @Test
    public void testSingle() throws Exception {
        System.out.println("[CLIENT] Beginning the first JTA transaction XXX");
        System.out.println("[CLIENT] Calling incrementCounter on the WS firstClient stub. The registered interceptor will bridge rom JTA to WS-AT");
        firstClient.incrementCounter(1);
        System.out.println("[CLIENT] Update successful, about to commit the JTA transaction. This will also cause the bridged WS-AT transaction to commit");
    }

    @Test
    public void testCommit() throws Exception {
        System.out.println("[CLIENT] Beginning the first JTA transaction");
        System.out.println("[CLIENT] Calling incrementCounter on the WS firstClient stub. The registered interceptor will bridge rom JTA to WS-AT");
        firstClient.incrementCounter(1);
        System.out.println("[CLIENT] Update successful, about to commit the JTA transaction. This will also cause the bridged WS-AT transaction to commit");

        System.out.println("[CLIENT] Beginning the second JTA transaction");
        System.out.println("[CLIENT] Calling getFirstCounter and getSecondCounter on the WS firstClient stub. The registered interceptor will bridge rom JTA to WS-AT");
        int counter1 = firstClient.getFirstCounter();
        int counter2 = firstClient.getSecondCounter();
        System.out.println("[CLIENT] Counters obtained successfully, about to commit the JTA transaction. This will also cause the bridged WS-AT transaction to commit");

        System.out.println("[CLIENT] Asserting that the counters were incremented successfully");
        Assert.assertEquals(1, counter1);
        Assert.assertEquals(1, counter2);
    }

    @Test
    public void testServiceDrivenRollback() throws Exception {
        System.out.println("[CLIENT] Beginning the first JTA transaction");
        System.out.println("[CLIENT] Calling incrementCounterAndRollBack on the WS firstClient stub. The registered interceptor will bridge rom JTA to WS-AT");
        firstClient.incrementCounterAndRollBack(1);
        System.out.println("[CLIENT] Update abd rollback successful.");

//             System.out.println("[CLIENT] Beginning the second JTA transaction");
//             System.out.println("[CLIENT] Calling getFirstCounter and getSecondCounter on the WS firstClient stub. The registered interceptor will bridge rom JTA to WS-AT");
//             int counter1 = firstClient.getFirstCounter();
//             int counter2 = firstClient.getSecondCounter();
//              System.out.println("[CLIENT] Counters obtained successfully, about to commit the JTA transaction. This will also cause the bridged WS-AT transaction to commit");
//
//            System.out.println("[CLIENT] Asserting that the counter increments were *not* successful");
//            Assert.assertEquals(0, counter1);
//            Assert.assertEquals(0, counter2);
    }
}