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

import javax.transaction.xa.XAResource;
import javax.transaction.Synchronization;
import javax.transaction.Transaction;
import javax.transaction.RollbackException;
import java.util.*;
import javax.transaction.xa.*;
import java.io.Serializable;

/**
 * Dummy implementation of an XA resource.
 * Useful for verifying that 2 phase commit is being used and for experimenting with errors during 2PC
 */
public class TestResource implements Synchronization, Serializable, XAResource
{
    private static int nrid = 0;

    private int rid = ++nrid;
    private int txTimeout = 300;
    private Set<Xid> _xids = new HashSet<Xid>();
    private String arg;

    /**
     * Initialize a dummy XA resource. arg is used to inject faults during 2PC:
     * fp means throw an exception during prepare
     * fc means throw an exception during commit
     * fr means throw an exception during rollback
     * @param arg a paramter for simulating faults
     */
    public TestResource(String arg)
    {
        this.arg = arg;
    }

    // Synchronizatons
    /**
     * called before the 2PC protocol runs
     */
    public void beforeCompletion()
    {
    }

    /**
     * called after the 2PC protocol completes
     * @param status the status of the transaction after it completed
     */
    public void afterCompletion(int status)
    {
    }

    // XA Interface implementation
    public int prepare(Xid xid) throws XAException
    {
        if ("fp".equals(arg))
            throw new XAException(XAException.XAER_RMFAIL);

        System.out.println("prepare resource " + rid);
        _xids.add(xid);

        return XA_OK;
    }

    public void commit(Xid xid, boolean b) throws XAException
    {
        if ("fc".equals(arg))
            throw new XAException(XAException.XAER_RMFAIL);

        System.out.println("commit resource " + rid);
        _xids.remove(xid);
    }

    public void rollback(Xid xid) throws XAException
    {
        if ("fr".equals(arg))
            throw new XAException(XAException.XAER_RMFAIL);

        System.out.println("rollback resource " + rid);
        _xids.remove(xid);
    }

    public void end(Xid xid, int i) throws XAException
    {
    }

    public void forget(Xid xid) throws XAException
    {
        _xids.remove(xid);
    }

    public int getTransactionTimeout() throws XAException
    {
        return txTimeout;
    }

    public boolean isSameRM(XAResource xaResource) throws XAException
    {
        return false;
    }

    public Xid[] recover(int i) throws XAException
    {
        return _xids.toArray(new Xid[_xids.size()]);
    }

    public boolean setTransactionTimeout(int txTimeout) throws XAException
    {
        this.txTimeout = txTimeout;

        return true;    // set was successfull
    }

    public void start(Xid xid, int i) throws XAException
    {
        _xids.add(xid);
    }

    /**
     * Enlist XA resources into the current transaction.
     * If any args match
     * work=fp - fail the prepare call
     * work=fc - fail the commit call
     * work=fr - fail the rollback call
     * then faults will be injected during 2PC
     * @param args optional args for inject 2PC faults
     */
    public static int checkArgs(String ... args)
    {
		int rcnt = 0;

        for (String arg : args)
            if (arg.startsWith("work="))
                rcnt += doWork(arg.split("=")[1]);

		return rcnt;
    }

    private static int doWork(String arg)
    {
        Transaction tx;

        System.out.println("doWork arg=" + arg);
        try
        {
            tx = com.arjuna.ats.jta.TransactionManager.transactionManager().getTransaction();
            try
            {
                tx.enlistResource(new TestResource(arg));

				return 1;
            }
            catch (RollbackException e)
            {
                e.printStackTrace();
            }
        }
        catch (javax.transaction.SystemException e)
        {
            e.printStackTrace();
        }

		return 0;
    }
}
