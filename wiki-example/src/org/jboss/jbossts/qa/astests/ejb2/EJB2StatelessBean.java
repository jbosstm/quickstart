package org.jboss.jbossts.qa.astests.ejb2;

import org.jboss.jbossts.qa.astests.ejbutil.Util2;

import jakarta.ejb.SessionBean;
import jakarta.ejb.SessionContext;

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