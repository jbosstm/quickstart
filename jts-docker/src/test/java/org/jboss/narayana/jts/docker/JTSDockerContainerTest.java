/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2015, Red Hat, Inc., and individual contributors
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

package org.jboss.narayana.jts.docker;

import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.orbportability.OA;
import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.Services;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.omg.CORBA.TRANSACTION_ROLLEDBACK;
import org.omg.CosTransactions.Control;
import org.omg.CosTransactions.Resource;
import org.omg.CosTransactions.TransactionFactory;
import org.omg.CosTransactions.TransactionFactoryHelper;

import java.util.Properties;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class JTSDockerContainerTest {

    /**
     * This has to be changed to the IP of the client, which would be accessible from Docker container
     */
    private static String CLIENT_IP = "";

    /**
     * This has to be changed to the IP of the JTS Docker image
     */
    private static String NAME_SERVER_IP = "";

    /**
     * This has to be changed to the PORT of the name service running inside JTS Docker image.
     *
     * Default is 3528.
     */
    private static String NAME_SERVER_PORT = "3528";

    private ORB testORB;

    private OA testOA;

    private TransactionFactory transactionFactory;

    @BeforeClass
    public static void beforeClass() {
        if (System.getProperty("CLIENT_IP") != null) {
            CLIENT_IP = System.getProperty("CLIENT_IP");
        }

        if (System.getProperty("NAME_SERVER_IP") != null) {
            NAME_SERVER_IP = System.getProperty("NAME_SERVER_IP");
        }

        if (System.getProperty("NAME_SERVER_PORT") != null) {
            NAME_SERVER_PORT = System.getProperty("NAME_SERVER_PORT");
        }

        Assert.assertTrue("Client IP is required", CLIENT_IP.length() > 0);
        Assert.assertTrue("Name server IP is required", NAME_SERVER_IP.length() > 0);
        Assert.assertTrue("Name server PORT is required", NAME_SERVER_PORT.length() > 0);
    }

    @Before
    public void before() throws Exception {
        /**
         * Initialise ORB
         */
        final Properties orbProperties = new Properties();
        orbProperties.setProperty("ORBInitRef.NameService", "corbaloc::" + NAME_SERVER_IP + ":" + NAME_SERVER_PORT
                + "/StandardNS/NameServer-POA/_root");
        orbProperties.setProperty("OAIAddr", CLIENT_IP);

        testORB = ORB.getInstance("test");
        testOA = OA.getRootOA(testORB);

        testORB.initORB(new String[] {}, orbProperties);
        testOA.initOA();

        ORBManager.setORB(testORB);
        ORBManager.setPOA(testOA);

        /**
         * Initialise transaction factory
         */
        final Services services = new Services(testORB);
        final int resolver = Services.getResolver();
        final String[] serviceParameters = new String[] { Services.otsKind };

        org.omg.CORBA.Object service = services.getService(Services.transactionService, serviceParameters, resolver);
        transactionFactory = TransactionFactoryHelper.narrow(service);
    }

    @After
    public void after() {
        testOA.destroy();
        testORB.shutdown();
    }

    @Test
    public void testCommit() throws Exception {
        System.out.println("JTSDockerContainerTest.testCommit");
        System.out.println("Begin transaction");
        Control control = transactionFactory.create(0);

        final TestResource firstResource = new TestResource(true);
        final TestResource secondResource = new TestResource(true);

        final Resource firstReference = firstResource.getReference();
        final Resource secondReference = secondResource.getReference();

        System.out.println("Enlist resources");
        control.get_coordinator().register_resource(firstReference);
        control.get_coordinator().register_resource(secondReference);

        System.out.println("Commit transaction");
        control.get_terminator().commit(true);
    }

    @Test
    public void testRollback() throws Exception {
        System.out.println("JTSDockerContainerTest.testRollback");
        System.out.println("Begin transaction");
        Control control = transactionFactory.create(0);

        final TestResource firstResource = new TestResource(true);
        final TestResource secondResource = new TestResource(true);

        final Resource firstReference = firstResource.getReference();
        final Resource secondReference = secondResource.getReference();

        System.out.println("Enlist resources");
        control.get_coordinator().register_resource(firstReference);
        control.get_coordinator().register_resource(secondReference);

        System.out.println("Rollback transaction");
        control.get_terminator().rollback();
    }

    @Test
    public void testResourceRollback() throws Exception {
        System.out.println("JTSDockerContainerTest.testResourceRollback");
        System.out.println("Begin transaction");
        Control control = transactionFactory.create(0);

        final TestResource firstResource = new TestResource(true);
        final TestResource secondResource = new TestResource(false);

        final Resource firstReference = firstResource.getReference();
        final Resource secondReference = secondResource.getReference();

        System.out.println("Enlist resources");
        control.get_coordinator().register_resource(firstReference);
        control.get_coordinator().register_resource(secondReference);

        System.out.println("Commit transaction");

        try {
            control.get_terminator().commit(true);
            Assert.fail("TRANSACTION_ROLLEDBACK exception is expected");
        } catch (TRANSACTION_ROLLEDBACK e) {
            // Expected
        }
    }


}
