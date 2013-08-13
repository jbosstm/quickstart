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

import java.rmi.RemoteException;

public interface EJB2Remote extends javax.ejb.EJBObject
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
