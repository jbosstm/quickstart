package org.jboss.jbossts.qa.astests.ejb2;

import jakarta.ejb.CreateException;
import jakarta.ejb.EJBHome;
import java.rmi.RemoteException;

public interface EJB2Home extends EJBHome
{
    EJB2Remote create() throws CreateException, RemoteException;
}