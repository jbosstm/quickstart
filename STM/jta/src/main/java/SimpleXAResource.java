import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

public class SimpleXAResource implements XAResource {
    private boolean rolledBack;

    public boolean isRolledBack() {
        return rolledBack;
    }

    public void commit(Xid arg0, boolean arg1) throws XAException {
    }

    public void end(Xid arg0, int arg1) throws XAException {
    }

    public void forget(Xid arg0) throws XAException {
    }

    public int getTransactionTimeout() throws XAException {
        return 0;
    }

    public boolean isSameRM(XAResource arg0) throws XAException {
        return false;
    }

    public int prepare(Xid arg0) throws XAException {
        return XAResource.XA_OK;
    }

    public Xid[] recover(int arg0) throws XAException {
        return null;
    }

    public void rollback(Xid arg0) throws XAException {
        rolledBack = true;
    }

    public boolean setTransactionTimeout(int arg0) throws XAException {
        return false;
    }

    public void start(Xid arg0, int arg1) throws XAException {
    }
}