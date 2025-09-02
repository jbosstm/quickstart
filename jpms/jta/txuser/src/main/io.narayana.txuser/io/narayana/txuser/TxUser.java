package io.narayana.txuser;

import jakarta.transaction.HeuristicMixedException;
import jakarta.transaction.HeuristicRollbackException;
import jakarta.transaction.NotSupportedException;
import jakarta.transaction.RollbackException;
import jakarta.transaction.SystemException;
import jakarta.transaction.Transaction;
import jakarta.transaction.TransactionManager;
import jakarta.transaction.UserTransaction;

import javax.transaction.xa.XAResource;

public class TxUser {
    public void setTransactionTimeout(int timeout) throws TxUserException {
        TransactionManager tm = com.arjuna.ats.jta.TransactionManager.transactionManager();
        try {
            tm.setTransactionTimeout(timeout);
        } catch (SystemException e) {
            throw new TxUserException(e);
        }
    }

    public UserTransaction startTransaction() throws TxUserException {
        UserTransaction utx = com.arjuna.ats.jta.UserTransaction.userTransaction();
        try {
            utx.setTransactionTimeout(300);
            utx.begin();
            return utx;
        } catch (SystemException | NotSupportedException e) {
            throw new TxUserException(e);
        }
    }

    public void enlistResources(UserTransaction txn, XAResource ... resources) throws TxUserException {
        TransactionManager tm = com.arjuna.ats.jta.TransactionManager.transactionManager();
        try {
            Transaction tx = tm.getTransaction();

            for (XAResource resource : resources) {
                tx.enlistResource(resource);
            }
        } catch (SystemException | RollbackException e) {
            throw new TxUserException(e);
        }
    }

    public void endTransaction(UserTransaction txn) throws TxUserException {
        try {
            txn.commit();
        } catch (RollbackException | HeuristicMixedException | HeuristicRollbackException | SystemException e) {
            throw new TxUserException(e);
        }
    }

    public int getStatus(UserTransaction utx) throws TxUserException {
        try {
            return utx.getStatus();
        } catch (SystemException e) {
            throw new TxUserException(e);
        }
    }
}
