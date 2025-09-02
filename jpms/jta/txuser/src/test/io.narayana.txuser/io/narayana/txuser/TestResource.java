package io.narayana.txuser;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

public class TestResource implements XAResource {
    private final boolean failCommit;
    private final boolean failPrepare;

    int commitCount;
    int rollbackCount;
    int prepareCount;

    public TestResource() {
        this.failCommit = false;
        this.failPrepare = false;
    }

    public TestResource(boolean failPrepare) {
        this.failCommit = false;
        this.failPrepare = failPrepare;
    }

    @Override
    public void commit(Xid xid, boolean b) throws XAException {
        commitCount += 1;
        if (failCommit) {
            throw new XAException(XAException.XAER_RMFAIL);
        }
    }

    @Override
    public void end(Xid xid, int i) throws XAException {
    }

    @Override
    public void forget(Xid xid) throws XAException {
    }

    @Override
    public int getTransactionTimeout() throws XAException {
        return 0;
    }

    @Override
    public boolean isSameRM(XAResource xaResource) throws XAException {
        return false;
    }

    @Override
    public int prepare(Xid xid) throws XAException {
        prepareCount += 1;
        if (failPrepare) {
            throw new XAException(XAException.XAER_RMFAIL);
        }
        return 0;
    }

    @Override
    public Xid[] recover(int i) throws XAException {
        return new Xid[0];
    }

    @Override
    public void rollback(Xid xid) throws XAException {
        rollbackCount += 1;
    }

    @Override
    public boolean setTransactionTimeout(int i) throws XAException {
        return false;
    }

    @Override
    public void start(Xid xid, int i) throws XAException {
    }
}
