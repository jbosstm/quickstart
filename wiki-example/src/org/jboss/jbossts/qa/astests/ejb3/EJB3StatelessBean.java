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
package org.jboss.jbossts.qa.astests.ejb3;

//import org.jboss.ejb3.annotation.defaults.RemoteBindingDefaults;
import org.jboss.ejb3.annotation.RemoteBinding;
import org.jboss.jbossts.qa.astests.ejbutil.TestResource;

/*  AS5 only
import org.jboss.annotation.ejb.RemoteBinding;  // AS4 only
*/
import javax.ejb.Stateless;
import javax.ejb.Remote;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.RollbackException;

import org.jboss.jbossts.qa.astests.ejbutil.Util2;

@Stateless
@Remote(EJB3Remote.class)
// JBoss specific binding replaces jboss.xml invokers cfg
//@RemoteBinding(factory= RemoteBindingDefaults.PROXY_FACTORY_IMPLEMENTATION_IOR, jndiBinding="TestIIOPBean")
//@javax.ejb.TransactionManagement(TransactionManagementType.CONTAINER)
public class EJB3StatelessBean
{
//	@org.jboss.ejb3.annotation.JndiInject(jndiName="java:/TransactionManager") TransactionManager tm;

	/**
	 * @param host if non null then propagate the request to another EJB runing on host
	 * @param orbName the name of the orb providing naming contexts (defaults to JBoss)
	 * @param ots flag to indicate whether ejb invocations use JRMP or IIOP
	 * @param ejb3 if the request is to be forward to an ejb on host and ejb3 is true then
	 *        send the request to an EJB3. If it is false then send it to an EJB2.
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public String foo(String host, String port, String orbName, boolean ots, boolean ejb3, String ... args)
	{
		StringBuilder sb = Util2.parseEJBArgs(new StringBuilder("\tEJB3 foo"), host, args);

System.out.printf("EJB3: foo host=%s and %s (ejb3=%b) %n", host != null ? host : "null", sb, ejb3);
		if (host != null) {
System.out.printf("INVOKING remote foo2%n");
			Util2.invokeFoo2(sb, port, host, orbName, ots, ejb3, args);
		}

		return sb.toString();
	}

	@TransactionAttribute(TransactionAttributeType.MANDATORY)
	public String foo2(String ... args)
	{
		StringBuilder sb = Util2.parseEJBArgs(new StringBuilder("EJB3 foo2 invoked"), null, args);

System.out.printf("EJB3: foo2 and %s%n", sb);
		return sb.toString();
	}
}
