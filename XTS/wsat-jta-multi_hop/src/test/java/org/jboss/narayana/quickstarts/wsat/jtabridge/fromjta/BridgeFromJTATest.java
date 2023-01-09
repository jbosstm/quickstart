/*
 * JBoss, Home of Professional Open Source
 * Copyright 2016, Red Hat, Inc., and others contributors as indicated
 * by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */
package org.jboss.narayana.quickstarts.wsat.jtabridge.fromjta;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.narayana.quickstarts.wsat.jtabridge.first.FirstServiceATImpl;
import org.jboss.narayana.quickstarts.wsat.jtabridge.first.jaxws.FirstServiceAT;
import org.jboss.narayana.quickstarts.wsat.jtabridge.second.SecondServiceATImpl;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.naming.Context;
import javax.naming.InitialContext;
import jakarta.transaction.UserTransaction;

import java.io.File;

/**
 * Simple set of tests for the FirstServiceAT
 *
 * @author paul.robinson@redhat.com, 2012-01-04
 */
@RunWith(Arquillian.class)
public class BridgeFromJTATest {

    private static final String ManifestMF = "Manifest-Version: 1.0\n"
            + "Dependencies: org.jboss.xts,org.jboss.jts\n";

    private UserTransaction ut;
    private FirstServiceAT firstClient;

    /**
     * Create the deployment archive to be deployed by Arquillian.
     *
     * @return a JavaArchive representing the required deployment
     */
    @Deployment
    public static WebArchive createTestArchive() {

        WebArchive archive = ShrinkWrap.create(WebArchive.class, "bridge.war")
                .addPackages(true, FirstServiceATImpl.class.getPackage())
                .addPackages(true, SecondServiceATImpl.class.getPackage())
                .addPackages(true, FirstClient.class.getPackage())
                .addAsWebInfResource(new File("src/main/resources/META-INF/persistence.xml"), "classes/META-INF/persistence.xml");

        archive.setManifest(new StringAsset(ManifestMF));

        return archive;
    }


    @Before
    public void setupTest() throws Exception {
        Context initialContext = new InitialContext();
        ut = (UserTransaction)initialContext.lookup("java:comp/UserTransaction");
        firstClient = FirstClient.newInstance();
    }

    @After
    public void teardownTest() throws Exception {
        rollbackIfActive(ut);
        try {
            ut.begin();
            firstClient.resetCounter();
            ut.commit();
        } finally {
            rollbackIfActive(ut);
        }
    }
    @Test
    public void testSingle() throws Exception {
        System.out.println("[CLIENT] Beginning the first JTA transaction XXX");
        ut.begin();
        System.out.println("[CLIENT] Calling incrementCounter on the WS firstClient stub. The registered interceptor will bridge rom JTA to WS-AT");
        firstClient.incrementCounter(1);
        System.out.println("[CLIENT] Update successful, about to commit the JTA transaction. This will also cause the bridged WS-AT transaction to commit");
        ut.commit();
    }

    @Test
    public void testCommit() throws Exception {
        System.out.println("[CLIENT] Beginning the first JTA transaction");
        ut.begin();
        System.out.println("[CLIENT] Calling incrementCounter on the WS firstClient stub. The registered interceptor will bridge rom JTA to WS-AT");
        firstClient.incrementCounter(1);
        System.out.println("[CLIENT] Update successful, about to commit the JTA transaction. This will also cause the bridged WS-AT transaction to commit");
        ut.commit();

        System.out.println("[CLIENT] Beginning the second JTA transaction");
        ut.begin();
        System.out.println("[CLIENT] Calling getFirstCounter and getSecondCounter on the WS firstClient stub. The registered interceptor will bridge rom JTA to WS-AT");
        int counter1 = firstClient.getFirstCounter();
        int counter2 = firstClient.getSecondCounter();
        System.out.println("[CLIENT] Counters obtained successfully, about to commit the JTA transaction. This will also cause the bridged WS-AT transaction to commit");
        ut.commit();

        System.out.println("[CLIENT] Asserting that the counters were incremented successfully");
        Assert.assertEquals(1, counter1);
        Assert.assertEquals(1, counter2);
    }

    @Test
    public void testClientDrivenRollback() throws Exception {
        System.out.println("[CLIENT] Beginning the first JTA transaction");
        ut.begin();
        System.out.println("[CLIENT] Calling incrementCounter on the WS firstClient stub. The registered interceptor will bridge rom JTA to WS-AT");
        firstClient.incrementCounter(1);
        System.out.println("[CLIENT] Update successful, about to rollback the JTA transaction. This will also cause the bridged WS-AT transaction to rollback");
        ut.rollback();

        System.out.println("[CLIENT] Beginning the second JTA transaction");
        ut.begin();
        System.out.println("[CLIENT] Calling getFirstCounter and getSecondCounter on the WS firstClient stub. The registered interceptor will bridge rom JTA to WS-AT");
        int counter1 = firstClient.getFirstCounter();
        int counter2 = firstClient.getSecondCounter();
        System.out.println("[CLIENT] Counters obtained successfully, about to commit the JTA transaction. This will also cause the bridged WS-AT transaction to commit");
        ut.commit();

        System.out.println("[CLIENT] Asserting that the counter increments were *not* successful");
        Assert.assertEquals(0, counter1);
        Assert.assertEquals(0, counter2);
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
