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
package org.jboss.jbossts.qa.astests.ejb2;

import org.jboss.jbossts.qa.astests.ejbutil.Util2;

import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

public class EJB2StatelessBean implements SessionBean
{
	// EJB method with the REQUIRED TransactionAttributeType
	public String foo(String host, String port, String orbName, boolean ots, boolean ejb3, String ... args)
	{
		StringBuilder sb = Util2.parseEJBArgs(new StringBuilder("\tEJB2 foo"), host, args);

System.out.printf("foo host=%s and %s ejb3=%b%n", host != null ? host : "null", sb, ejb3);
		if (host != null) {
System.out.printf("INVOKING remote foo2%n");
			Util2.invokeFoo2(sb, host, port, orbName, ots, ejb3, args);
		}

		return sb.toString();
	}

	// EJB method with the MANDATORY TransactionAttributeType
    public String foo2(String ... args)
    {
        StringBuilder sb = Util2.parseEJBArgs(new StringBuilder("EJB2 foo2 method"), null, args);

System.out.printf("foo2 and %s%n", sb);
		return sb.toString();
    }

	public void setSessionContext(SessionContext context) { }
	public void ejbCreate() { }
	public void ejbActivate() { }
	public void ejbPassivate() { }
	public void ejbRemove() { }
}
