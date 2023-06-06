package io.narayana.util;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

/**
 * Failing on commit call with {@link XAException}.
 * Using in test of recovery of the resource works.
 */
public class FailingXAResource implements XAResource {

    @Override
    public void commit(Xid arg0, boolean arg1) throws XAException {
        throw new XAException(XAException.XAER_RMFAIL);
    }

    @Override
    public void end(Xid arg0, int arg1) throws XAException {
    }

    @Override
    public void forget(Xid arg0) throws XAException {
    }

    @Override
    public int getTransactionTimeout() throws XAException {
        return 0;
    }

    @Override
    public boolean isSameRM(XAResource arg0) throws XAException {
        return false;
    }

    @Override
    public int prepare(Xid arg0) throws XAException {
        return 0;
    }

    @Override
    public Xid[] recover(int arg0) throws XAException {
        return null;
    }

    @Override
    public void rollback(Xid arg0) throws XAException {
    }

    @Override
    public boolean setTransactionTimeout(int arg0) throws XAException {
        return false;
    }

    @Override
    public void start(Xid arg0, int arg1) throws XAException {
    }
}