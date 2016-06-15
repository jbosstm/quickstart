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
package org.jboss.jbossts.qa.astests.ejbutil;

import com.arjuna.orbportability.ORB;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import java.io.IOException;
import java.util.Properties;

import org.omg.CORBA.ORBPackage.InvalidName;

/**
 * Useful routines for looking up JNDI names
 */

public class Util2
{
	//private static String EJB2_JNDI_CORBA_URL = "corbaloc::HOST:3528/ORB/Naming/root";
	private static String EJB2_JNDI_CORBA_URL = "corbaloc::HOST:PORT/NameService";
    private static String JNDI_URL = "jnp://HOST:PORT";

	/**
	 * get a naming context
	 * @param host the host on which the target JNDI service is running
	 * @param orbName the name of the orb (required when trying to obtain a CORBA naming context)
	 * @param useOts set to true to obtain a CORBA naming context (otherwise jnp is used)
	 * @return the desired context
	 * @throws NamingException
	 * @throws IOException
	 * @throws InvalidName
	 */
    public static Context getNamingContext(boolean useOTS, String host, String port, String orbName) throws NamingException, IOException
    {
        Properties properties = new Properties();
        String url = useOTS ? EJB2_JNDI_CORBA_URL : JNDI_URL;

        url = url.replace("HOST", host).replace("ORB", orbName).replace("PORT", port);

//        System.out.println("jndi url: " + url);
        properties.setProperty(Context.PROVIDER_URL, url);

	if (useOTS) {
		org.omg.CORBA.ORB norb = org.jboss.iiop.naming.ORBInitialContextFactory.getORB();
		// if norb is not null then we are running inside the AS so make sure that its root name context
		// is used in preferenance to the one defined by Context.PROVIDER_URL
		if (norb != null)
			properties.put("java.naming.corba.orb", norb); 

    		properties.setProperty(Context.URL_PKG_PREFIXES, "org.jboss.iiop.naming:org.jboss.naming.client:org.jnp.interfaces");
        	properties.setProperty(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.cosnaming.CNCtxFactory");
			properties.put(Context.OBJECT_FACTORIES, "org.jboss.tm.iiop.client.IIOPClientUserTransactionObjectFactory");
	} else {
    		properties.setProperty(Context.URL_PKG_PREFIXES, "org.jboss.naming.client:org.jnp.interfaces");
        	properties.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.jboss.naming.NamingContextFactory");
		}

        return new InitialContext(properties);
    }

	/**
	 * obtain an object via JNDI
	 * @param host host on which the JNDI service is running
	 * @param orbName name of orb hosting a CORBA naming service
	 * @param useOTS whether to use a CORBA naming service
	 * @param name the JNDI name of the target object
	 * @param clazz class of the target object
	 * @return an object whose name is bound in a name service running on the specified host
	 * @throws Exception
	 */
    public static Object lookupObject(boolean useOTS, String host, String port, String orbName, String name) throws Exception
    {
//		System.out.println("lookup EJB " + name + " at host " + host + " using orb " + (useOTS ? orbName : "none"));

        Context ctx = getNamingContext(useOTS, host, port, orbName);

        return ctx.lookup(name);
    }

    public static Object lookupEjb(boolean useOTS, String host, String port, String orbName, String name, Class clazz) throws Exception
    {
		try
		{
        	return PortableRemoteObject.narrow(lookupObject(useOTS, host, port, orbName, name), clazz);
		}
		catch (Exception e)
		{
			System.out.println("lookupEjb error: " + e.getMessage() + " host=" + host + " jndiName: " + name);
			throw e;
		}
    }

	/**
	 * Parse args from looking for dummy XA resources to regiter with the current transaction.
	 *
	 * @param sb StringBuild for holding a printable message
	 * @param host the host that the EJB request should be forwarded to
	 * @param args arbitrary args associated with an EJB invocation
	 * @return the passed in StringBuilder
	 */
	public static StringBuilder parseEJBArgs(StringBuilder sb, String host, String ... args)
	{
		int rcnt = TestResource.checkArgs(args);

		for (int i = 0; i < args.length; i++) {
			if (args[i].startsWith("from=")) {
				sb.append(" invocation: ").append(args[i].split("=")[1]);
				sb.append(" -> ").append(System.getProperty("jboss.bind.address", "ThisHost"));
				args[i] = "from=" + System.getProperty("jboss.bind.address", "OtherHost");
			}
		}

		sb.append(" (enlisting ").append(rcnt).append(" XA resources)");

		return sb;
	}

    public static void invokeFoo2(StringBuilder sb, String host, String port, String orbName,
		boolean ots, boolean ejb3, String ... args)
	{
		String jndiName;

		if (ejb3)
//           jndiName = "TestIIOPBean";
		   jndiName = "EJB3StatelessBean/remote";
		else if (ots)
			jndiName = "OTSEjb2StatelessBean";
		else
			jndiName = "Ejb2StatelessBean";

		sb.append((ejb3 ? "... EJB3 " : "... EJB2 ")).append("foo2").append(" response: ");

        try
        {
			if (ejb3) {
            	org.jboss.jbossts.qa.astests.ejb3.EJB3Remote rem = (org.jboss.jbossts.qa.astests.ejb3.EJB3Remote)
				lookupEjb(ots, host, port, orbName, jndiName, org.jboss.jbossts.qa.astests.ejb3.EJB3Remote.class);

				sb.append(rem.foo2(args));
			} else {
				org.jboss.jbossts.qa.astests.ejb2.EJB2Home home = (org.jboss.jbossts.qa.astests.ejb2.EJB2Home)
				lookupEjb(ots, host, port, orbName, jndiName, org.jboss.jbossts.qa.astests.ejb2.EJB2Home.class);
				org.jboss.jbossts.qa.astests.ejb2.EJB2Remote rem = home.create();
	
				sb.append(rem.foo2(args));
			}
        }
        catch (Exception e)
        {
            sb.append("exception: ").append(e.getMessage()).append(": host=").append(host)
				.append(" orbName=").append(orbName).append(" ots=").append(ots).append(" jndi=" ).append(jndiName);
			e.printStackTrace();
		}
	}
}
