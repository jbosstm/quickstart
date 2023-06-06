package org.jboss.narayana.jta.quickstarts;

import jakarta.transaction.HeuristicMixedException;
import jakarta.transaction.HeuristicRollbackException;
import jakarta.transaction.NotSupportedException;
import jakarta.transaction.RollbackException;
import jakarta.transaction.Status;
import jakarta.transaction.SystemException;
import jakarta.transaction.TransactionManager;
import jakarta.transaction.UserTransaction;

import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.orbportability.OA;
import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.RootOA;
import com.arjuna.orbportability.common.OrbPortabilityEnvironmentBean;
import com.arjuna.orbportability.common.OrbPortabilityEnvironmentBeanMBean;
import org.omg.CORBA.ORBPackage.InvalidName;
import com.arjuna.common.internal.util.propertyservice.BeanPopulator;
import java.util.Properties;

// Based on a quickstart in the wildfly quickstart repo: https://github.com/jbosstm/quickstart/tree/main/ArjunaJTA/javax_transaction
public class TransactionExample {

    private static final String ORB_IMPL_PROP = "OrbPortabilityEnvironmentBean.orbImpleClassName";
    private static String OPENJDKORB_CLASSNAME = com.arjuna.orbportability.internal.orbspecific.javaidl.orb.implementations.javaidl_1_4.class.getName();

    public static void main(String[] args) throws Exception {
        System.setProperty("ObjectStoreBaseDir", "target");
        TransactionExample txeg = new TransactionExample();

        init(args);

        txeg.commitUserTransaction();
        txeg.commitTransactionManager();
        txeg.rollbackUserTransaction();
        txeg.setRollbackOnly();
        txeg.transactionStatus();
        txeg.transactionTimeout();
    }

    private static void init(String[] args) throws InvalidName {
        ORB myORB = ORB.getInstance("test");
        RootOA myOA = OA.getRootOA(myORB);
        String orbImpl = BeanPopulator.getDefaultInstance(OrbPortabilityEnvironmentBean.class).getOrbImpleClassName();

        if (OPENJDKORB_CLASSNAME.equals(orbImpl)) {
            System.out.printf("Testing against OpenJDK ORB%n");
        } else {
            throw new RuntimeException("please configure an orb using the property " + ORB_IMPL_PROP);
        }

        final Properties p = new Properties();

        myORB.initORB((String[]) null, p);
        myOA.initOA();

        ORBManager.setORB(myORB);
        ORBManager.setPOA(myOA);
    }

    public void commitUserTransaction() throws SystemException, NotSupportedException, RollbackException, HeuristicRollbackException, HeuristicMixedException {
        //get UserTransaction
        UserTransaction utx = com.arjuna.ats.jta.UserTransaction.userTransaction();

        // start transaction
        utx.begin();
        // ... do some transactional work ...
        // commit it
        utx.commit();
    }

    public void commitTransactionManager() throws NotSupportedException, RollbackException, SystemException, HeuristicMixedException, HeuristicRollbackException {
        //get TransactionManager
        TransactionManager tm = com.arjuna.ats.jta.TransactionManager.transactionManager();

        // start a transaction by calling begin on the transaction manager
        tm.begin();

        tm.commit();
    }

    public void rollbackUserTransaction() throws SystemException, NotSupportedException {
        UserTransaction utx = com.arjuna.ats.jta.UserTransaction.userTransaction();

        utx.begin();

        // abort the transaction
        utx.rollback();
    }

    public void setRollbackOnly() throws SystemException, NotSupportedException, HeuristicRollbackException, HeuristicMixedException {
        //get TransactionManager
        TransactionManager tm = com.arjuna.ats.jta.TransactionManager.transactionManager();

        // start transaction work..
        tm.begin();

        // perform transactional work
        tm.setRollbackOnly();
        try {
            tm.commit();
            throw new RuntimeException("Should have got an exception whilst committing a transaction is marked as rollback only");
        } catch (RollbackException e) {
            // ignore
        }
    }

    public void transactionStatus() throws SystemException, NotSupportedException {
        UserTransaction utx = com.arjuna.ats.jta.UserTransaction.userTransaction();

        utx.begin();

        // abort the transaction
        if (utx.getStatus() != Status.STATUS_ACTIVE)
            throw new RuntimeException("transaction should have been active");
        utx.setRollbackOnly();
        if (utx.getStatus() != Status.STATUS_MARKED_ROLLBACK)
            throw new RuntimeException("transaction should have been marked rollback only");
        utx.rollback();
        if (utx.getStatus() != Status.STATUS_NO_TRANSACTION)
            throw new RuntimeException("transaction should not exist");
    }

    public void transactionTimeout() throws SystemException, NotSupportedException, InterruptedException, HeuristicRollbackException, HeuristicMixedException {
        UserTransaction utx = com.arjuna.ats.jta.UserTransaction.userTransaction();

        utx.setTransactionTimeout(1);
        utx.begin();
        Thread.sleep(1500);
        try {
            utx.commit();
            throw new RuntimeException("Should have got an exception whilst committing a transaction that exceeded its timeout");
        } catch (RollbackException e) {
            // ignore
        }
    }
}