/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
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
 *
 * (C) 2008
 * @author JBoss Inc.
 */
package com.arjuna.ats.tools;

import java.io.IOException;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

import javax.transaction.UserTransaction;

// imports for the case where the client controls the distributed transaction
// these are not need if the client is using JNDI to look up the UserTransaction object
import org.omg.CORBA.ORBPackage.InvalidName;
import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.RootOA;
import com.arjuna.orbportability.OA;
import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import com.arjuna.ats.jts.OTSManager;

import org.jboss.jbossts.qa.astests.ejbutil.Util2;

import org.jboss.jbossts.qa.astests.ejb2.*;
import org.jboss.jbossts.qa.astests.ejb3.*;

/**
 * Class to test client initiated transactions
 */
public class TxTest
{
	private static String EJB2_JNDI_CORBA_FAC = "com.sun.jndi.cosnaming.CNCtxFactory";
	// use the CORBA name service to do JNDI lookups
	// HOST is the host where the EJB has been registered
	// ORB is the implementation name of the ORB (default name is JBoss
	// - see conf/jacorb.properties property jacorb.implname)
	// the port 3528 is the IIOP port officially assigned to JBoss by IANA
	private static String EJB2_JNDI_CORBA_URL = "corbaloc::HOST:3528/ORB/Naming/root";

	private static String JNDI_FAC = "org.jboss.naming.NamingContextFactory";  //"org.jnp.interfaces.NamingContextFactory"
	private static String JNDI_URL = "HOST:1099/NameService";
	private static String JNDI_PKGS = "org.jboss.naming.client:org.jnp.interfaces";

	// use the JTS version of the JBossTM product
	private static boolean useOTS = false;
	private static ORBWrapper orbWrapper;

	// user transaction obtained from the JBoss ClientUserTransactionService
	private static UserTransaction uts;

	private static String[] hosts = {"default ip", "default ip", "localhost"};
	private static String[] corbaPorts = {"3528", "3628", "3528"};
	private static String[] jndiPorts = {"1099", "1199", "1099"};
	private static String work = "work=true";

	private static boolean testIIOP;
	private StringBuilder summary = new StringBuilder();

	public static void main(String[] args) throws Exception
	{
		TxTest ot = new TxTest();
		String test = null;

		for (String arg : args)
		{
			if (arg.startsWith("host.from="))
				hosts[0] = arg.split("=")[1];
			else if (arg.startsWith("host.to="))
				hosts[1] = arg.split("=")[1];
			else if (arg.startsWith("test="))
				test = (arg.length() == 5 ? null : arg.split("=")[1]);
//			else if (arg.startsWith("storeDir="))
//				com.arjuna.ats.arjuna.common.arjPropertyManager.getObjectStoreEnvironmentBean().
//					setObjectStoreDir(arg.split("=")[1]);
			else if (arg.startsWith("iiop=") && arg.split("=").length > 1)
				testIIOP = true;
			else if (arg.startsWith("work="))
				work = arg;
		}

		// Client User Transaction Tests
		if ("1".equals(test) || test == null)
			ot.doTest("1", false, false, true, true, args); // EJB2 (different servers)
		if ("2".equals(test) || test == null)
			ot.doTest("2", false, false, false, true, args);// EJB2 (server propagates tx to other server)
		if ("2b".equals(test) || test == null)
			ot.doTest("2b", false, false, false, false, args);// EJB2 (server propagates tx to other server)

		if ("3".equals(test) || test == null)
			ot.doTest("3", false, true, true, true, args);  // EJB3 over JRMP (different servers)
		if ("4".equals(test) || test == null)
			ot.doTest("4", false, true, false, true, args); // EJB3 over JRMP (server propagates tx to other server)
		if ("4b".equals(test) || test == null)
			ot.doTest("4b", false, true, false, false, args); // EJB3 over JRMP (server propagates tx to other server)

		// OTS Transaction Tests
		if ("5".equals(test) || test == null)
			ot.doTest("5", true, false, true, true, args);  // EJB2 (different servers)
		if ("6".equals(test) || test == null)
			ot.doTest("6", true, false, false, true, args); // EJB2 (server propagates tx to other server)
		if ("6b".equals(test) || test == null)
			ot.doTest("6b", true, false, false, false, args); // EJB2 (server propagates tx to other server)

		// tests 7 and 8 fail since EJB3 over IIOP is not supported in AS5
		if (testIIOP)
		{
			if ("7".equals(test) || test == null)
				ot.doTest("7", true, true, false, true, args);  // EJB3 over IIOP (different servers)
			if ("8".equals(test) || test == null)
				ot.doTest("8", true, true, true, true, args);   // EJB3 over IIOP (server propagates tx to other server)
			if ("8b".equals(test) || test == null)
				ot.doTest("8b", true, true, true, false, args);   // EJB3 over IIOP (server propagates tx to other server)
		}

		ot.report();
		
		RecoveryManager.manager().terminate();
	}

	/**
	 * Start a test
	 * @param ots use OTS or Client User Transaction
	 * @param ejb3 use EJB2 or EJB3 model
	 * @param as call EJBs in different servers if true. If false call EJB in one server and
	 * ask it to propagate the request to a second server.
	 * @param args various arg to control the behaviour of the EJBs when invoked
	 * @throws Exception
	 */
	private void doTest(String testid, boolean ots, boolean ejb3, boolean as, boolean tx, String[] args) throws Exception
	{
		System.out.print("TEST ID " + testid + " - ");
		if (tx)
			System.out.print("CLIENT INITIATED TRANSACTION (VIA " + (ots ? "OTS):" : "JNDI LOOKUP):"));
		else
			System.out.print("SERVER INITIATED TRANSACTION:");

		System.out.print((ejb3 ? " EJB3 CALL" : " EJB2 CALL"));
		System.out.println((as ? " (TWO SERVERS)" : " (TO 1 SERVER WHICH IN TURN CALLS ANOTHER)"));

		useOTS = ots;

		if (!ots)
			uts = (UserTransaction) Util2.lookupObject(ots, hosts[0], jndiPorts[0],"JBoss", "UserTransaction");

		if (ots || ejb3)
		{
			// if alwaysPropagateContext is not set via the properties file then do it via the API:
//			com.arjuna.ats.jts.common.jtsPropertyManager.getJTSEnvironmentBean().setAlwaysPropagateContext(true);
			orbWrapper = new ORBWrapper();
			orbWrapper.start();
		}
		
		RecoveryManager.manager();

		ejbTest(testid, ots, ejb3, as, tx, args);

		if (orbWrapper != null)
			orbWrapper.stop();
	}

	/**
	 * start a transaction
	 * @param ots whether to use the OTS transaction model or ClientUserTransaction
	 * @throws Exception
	 */
	private void startTx(boolean ots) throws Exception
	{
		if (ots)
			OTSManager.get_current().begin();
		else
			uts.begin();
	}

	/**
	 * end a transaction
	 * @param ots whether to use the OTS transaction model or ClientUserTransaction
	 * @throws Exception
	 */
	private void endTx(boolean ots) throws Exception
	{
		if (ots)
			OTSManager.get_current().commit(true);
		else
			uts.commit();
	}

	/**
	 * @see doTest
	 */
	private void ejbTest(String testid, boolean ots, boolean ejb3, boolean as, boolean tx, String[] args) throws Exception
	{
//		System.out.println("> Starting " + (useOTS ? "OTS tx with ejb" : "CUT with ejb") + (ejb3 ? "3" : "2"));
		String[] orbs = {"JBoss", "JBoss"};
		String port = ots ? corbaPorts[0] : jndiPorts[0];
		String port2 = ots ? corbaPorts[1] : jndiPorts[1];

		if (tx)
			startTx(ots);

		try {
			if (ejb3)
			{
//               String jndiName = "TestIIOPBean";
               String jndiName = "EJB3StatelessBean/remote";

				if (as)
				{
					EJB3Remote bean1 = (EJB3Remote) Util2.lookupEjb(ots, hosts[0], port, orbs[0], jndiName, EJB3Remote.class);
					report(testid, bean1.foo(null, null, null, ots, ejb3, work, "from=client"));

					EJB3Remote bean2 = (EJB3Remote) Util2.lookupEjb(ots, hosts[1], port, orbs[1], jndiName, EJB3Remote.class);
					report(testid, bean2.foo(null, null, null, ots, !ejb3, work, "from=client"));
				}
				else
				{
					EJB3Remote bean1 = (EJB3Remote) Util2.lookupEjb(ots, hosts[0], port,orbs[0], jndiName, EJB3Remote.class);

					report(testid, bean1.foo(hosts[1], port2, orbs[1], ots, ejb3, work, "from=client"));
					report(testid, bean1.foo(hosts[1], port2, orbs[1], ots, !ejb3, work, "from=client"));
				}
			}
			else
			{
				String jndiName = ots ? "OTSEjb2StatelessBean" : "Ejb2StatelessBean";

				if (as)
				{
					EJB2Home home1 = (EJB2Home) Util2.lookupEjb(ots, hosts[0], port,orbs[0], jndiName, EJB2Home.class);
					EJB2Remote bean1 = home1.create();
					report(testid, bean1.foo(null, null, null, ots, ejb3, work, "from=client"));

					EJB2Home home2 = (EJB2Home) Util2.lookupEjb(ots, hosts[1], port,orbs[1], jndiName, EJB2Home.class);
					EJB2Remote bean2 = home2.create();
					report(testid, bean2.foo(null, null, null, ots, !ejb3, work, "from=client"));
				}
				else
				{
					EJB2Home home = (EJB2Home) Util2.lookupEjb(ots, hosts[0], port,orbs[0], jndiName, EJB2Home.class);
					EJB2Remote bean1 = home.create();

					report(testid, bean1.foo(hosts[1], port2, orbs[1], ots, ejb3, work, "from=client"));
					// AS5 does not support IIOP for the EJB3 model so check that we are specifically testing it
					if (ots && testIIOP) {
						// request that the invoked EJB forwards the request to another EJB3 using IIOP
						report(testid, bean1.foo(hosts[1], port2, orbs[1], ots, !ejb3, work, "from=client"));
					}
				}
			}
		} catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (tx)
				endTx(useOTS);
		}
	}

	private void report(String testid, String output)
	{
		boolean passed = output.indexOf("exception") == -1;
		String result = String.format("Test %s %s%n",
			testid, (passed ? " PASSED" : " FAILED"));

		System.out.printf("Info: %s%s%n", result, output);

		summary.append(result);
	}

	private void report()
	{
		System.out.print(summary);
	}

	/**
	 * class to start and stop an ORB for use with OTS based transactions
	 */
	class ORBWrapper
	{
		ORB orb;
		RootOA oa;

		void start() throws InvalidName
		{
			orb = com.arjuna.orbportability.ORB.getInstance("ClientSide");
			oa = OA.getRootOA(orb);
			orb.initORB(new String[] {}, null);
			oa.initOA();

            com.arjuna.ats.internal.jts.ORBManager.setORB(orb);
            com.arjuna.ats.internal.jts.ORBManager.setPOA(oa);
		}

		void stop()
		{
			oa.destroy();
			orb.shutdown();
		}
	}
}

