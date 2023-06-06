package org.jboss.jbossts.qa.astests.ejb3;

import java.rmi.RemoteException;

/**
 * ejb3 version of MyRemote (@see org.jboss.jbossts.qa.astests.ejb2.MyRemote
 */
public interface EJB3Remote
{
    public String foo(String host, String port, String orb, boolean ots, boolean ejb3, String ... args) throws RemoteException;
	public String foo2(String ... args) throws RemoteException;
}