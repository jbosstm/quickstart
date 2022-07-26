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
package org.jboss.narayana.jta.quickstarts;

import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.common.internal.util.propertyservice.BeanPopulator;
import com.arjuna.orbportability.OA;
import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.Services;
import com.arjuna.orbportability.common.OrbPortabilityEnvironmentBean;
import org.omg.CosTransactions.Control;
import org.omg.CosTransactions.Resource;
import org.omg.CosTransactions.TransactionFactory;
import org.omg.CosTransactions.TransactionFactoryHelper;

import java.net.InetAddress;
import java.util.Properties;

/**
 * test code based on Gytis' JTS docker image
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class RemoteTMExample {
    private static String NAME_SERVER_HOST = null;
    private static String NAME_SERVER_PORT = "9999";

    private static final String ORB_IMPL_PROP = "OrbPortabilityEnvironmentBean.orbImpleClassName";
    private static final String OPENJDKORB_CLASSNAME = com.arjuna.orbportability.internal.orbspecific.javaidl.orb.implementations.javaidl_1_4.class.getName();

    private ORB testORB;
    private OA testOA;

    // OTS entry point for starting transactions
    private TransactionFactory transactionFactory;

    public static void main(String[] args) throws Exception {
        System.setProperty("ObjectStoreBaseDir", "target");
        // tell the TM to use a CORBA naming service to look up the TM
        System.setProperty("OrbPortabilityEnvironmentBean.bindMechanism", "NAME_SERVICE");
        System.setProperty("OrbPortabilityEnvironmentBean.resolveService", "NAME_SERVICE");

        NAME_SERVER_HOST = System.getProperty("NAME_SERVER_HOST", InetAddress.getLocalHost().getHostAddress());
        NAME_SERVER_PORT = System.getProperty("NAME_SERVER_PORT", NAME_SERVER_PORT);

        RemoteTMExample eg = new RemoteTMExample();

        eg.before();
        eg.testCommit();
        eg.after();
    }

    /* create and initialise a CORBA orb */
    public void before() throws Exception {
        final Properties orbProperties = new Properties();
        String[] args;
        String orbImpl = BeanPopulator.getDefaultInstance(OrbPortabilityEnvironmentBean.class).getOrbImpleClassName();

        testORB = ORB.getInstance("test");
        testOA = OA.getRootOA(testORB);

        if (OPENJDKORB_CLASSNAME.equals(orbImpl)) {
            System.out.printf("Testing against OpenJDK ORB%n");
            args = new String[]{
                    "-ORBInitialHost", NAME_SERVER_HOST, "-ORBInitialPort", NAME_SERVER_PORT
            };
        } else {
            throw new RuntimeException("please configure an orb using the property " + ORB_IMPL_PROP);
        }

        testORB.initORB(args, orbProperties);

        testOA.initOA();

        ORBManager.setORB(testORB);
        ORBManager.setPOA(testOA);

        /**
         * Obtain an OTS transaction factory
         */
        final Services services = new Services(testORB);
        final int resolver = Services.getResolver();
        final String[] serviceParameters = new String[] { Services.otsKind };

        org.omg.CORBA.Object service = services.getService(Services.transactionService, serviceParameters, resolver);
        transactionFactory = TransactionFactoryHelper.narrow(service);
    }

    public void after() {
        testOA.destroy();
        testORB.shutdown();
    }

    public void testCommit() throws Exception {
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
        System.out.println("Commit OK");
    }
}
