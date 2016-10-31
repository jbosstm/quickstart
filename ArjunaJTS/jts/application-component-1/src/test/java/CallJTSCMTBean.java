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
import java.rmi.RemoteException;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.as.quickstarts.cmt.jts.ejb.CustomerManagerEJB;
import org.jboss.as.quickstarts.cmt.jts.ejb.CustomerManagerEJBImpl;
import org.junit.Test;

public class CallJTSCMTBean {

	@Test
	public void test() throws NamingException, RemoteException {
		final Hashtable jndiProperties = new Hashtable();
		jndiProperties.put(Context.URL_PKG_PREFIXES,
				"org.jboss.ejb.client.naming");
		jndiProperties.put("java.naming.factory.initial", "org.jboss.as.naming.InitialContextFactory");
		final Context context = new InitialContext(jndiProperties);
		final String appName = "";
		final String moduleName = "jts-quickstart";
		final String distinctName = "";
		final String beanName = CustomerManagerEJBImpl.class.getSimpleName();
		final String viewClassName = CustomerManagerEJB.class.getName();
		final CustomerManagerEJB statelessRemoteCalculator = (CustomerManagerEJB) context
				.lookup("ejb:" + appName + "/" + moduleName + "/"
						+ distinctName + "/" + beanName + "!" + viewClassName);
		statelessRemoteCalculator.createCustomer("foo");
	}
}
