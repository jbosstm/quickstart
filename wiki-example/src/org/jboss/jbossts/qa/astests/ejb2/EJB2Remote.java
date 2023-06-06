package org.jboss.jbossts.qa.astests.ejb2;

import java.rmi.RemoteException;

public interface EJB2Remote extends jakarta.ejb.EJBObject
{
    /**
     * Simple EJB implementation
     * @param host if set then the same request is sent to an EJB running on this host
     * @param port port to use when doing lookups via jndi or a naming server 
     * @param orbName name of orb if using CORBA naming service
     * @param ots whether to use a CORBA naming service
     * @param ejb3 if the request is to be sent to another server then send it via EJB3 if true
     * 	(otherwise send to an EJB2)
     * @param args optional args (@see org.jboss.jbossts.qa.astests.ejbutil.TestResource)
     */
    public String foo(String host, String port, String orbName, boolean ots, boolean ejb3, String ... args) throws RemoteException;

    public String foo2(String ... args) throws RemoteException;
}