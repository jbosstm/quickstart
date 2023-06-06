package org.jboss.narayana.jta.quickstarts.recovery;

import jakarta.transaction.HeuristicMixedException;
import jakarta.transaction.HeuristicRollbackException;
import jakarta.transaction.NotSupportedException;
import jakarta.transaction.RollbackException;
import jakarta.transaction.SystemException;
import jakarta.transaction.TransactionManager;

import org.jboss.narayana.jta.quickstarts.util.DummyXAResource;

public class BasicXAExample extends RecoverySetup {
    public static void main(String[] args) throws Exception {
        startRecovery();
        new BasicXAExample().resourceEnlistment();
        stopRecovery();
    }

    public void resourceEnlistment() throws NotSupportedException, RollbackException, SystemException, HeuristicMixedException, HeuristicRollbackException {
        // obtain a reference to the transaction manager
        TransactionManager tm = com.arjuna.ats.jta.TransactionManager.transactionManager();
        DummyXAResource xares1 = new DummyXAResource(DummyXAResource.faultType.NONE);
        DummyXAResource xares2 = new DummyXAResource(DummyXAResource.faultType.NONE);

        // start a transaction
        tm.begin();

        // enlist some resources
        tm.getTransaction().enlistResource(xares1);
        tm.getTransaction().enlistResource(xares2);

        if (!xares1.startCalled)
            throw new RuntimeException("start should have called");

        // commit any transactional work that was done on the two dummy XA resources
        tm.commit();

        if (!xares1.endCalled)
            throw new RuntimeException("end should have called");
        if (!xares1.prepareCalled)
            throw new RuntimeException("prepare should have called");
        if (!xares1.commitCalled)
                throw new RuntimeException("commit should have called");
    }
}