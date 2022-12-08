/*
 * Copyright The Narayana Authors
 *
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package org.jboss.narayana.jta.quickstarts;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import java.io.Serializable;

public class DummyXAResource implements XAResource, Serializable {
    static final long serialVersionUID = 1;

    public DummyXAResource() {
    }

    public void commit(final Xid xid, final boolean arg1) throws XAException {
        System.out.println("DummyXAResource commit() called");
    }

    public void end(final Xid xid, final int arg1) throws XAException {
    }

    public void forget(final Xid xid) throws XAException {
    }

    public int getTransactionTimeout() throws XAException {
        return 0;
    }

    public boolean isSameRM(final XAResource arg0) throws XAException {
        return this.equals(arg0);
    }

    public int prepare(final Xid xid) throws XAException {
        return XAResource.XA_OK;
    }

    public Xid[] recover(final int arg0) throws XAException {
        return new Xid[0];
    }

    public void rollback(final Xid xid) throws XAException {
    }

    public void start(final Xid xid, final int arg1) throws XAException {
    }

    public boolean setTransactionTimeout(final int arg0) throws XAException {
        return false;
    }
}
