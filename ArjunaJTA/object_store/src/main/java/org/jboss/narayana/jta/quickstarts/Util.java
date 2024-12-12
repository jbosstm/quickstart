package org.jboss.narayana.jta.quickstarts;

import java.io.File;

import com.arjuna.ats.arjuna.common.ObjectStoreEnvironmentBean;
import com.arjuna.common.internal.util.propertyservice.BeanPopulator;

import jakarta.transaction.RollbackException;
import jakarta.transaction.SystemException;
import jakarta.transaction.TransactionManager;

public class Util {
    public static void emptyObjectStore() {
        String objectStoreDirName = BeanPopulator.getDefaultInstance(ObjectStoreEnvironmentBean.class).getObjectStoreDir();

        if (objectStoreDirName != null)
            Util.removeContents(new File(objectStoreDirName));
    }

    public static void removeContents(File directory)
    {
        if ((directory != null) &&
                directory.isDirectory() &&
                (!directory.getName().equals("")) &&
                (!directory.getName().equals("/")) &&
                (!directory.getName().equals("\\")) &&
                (!directory.getName().equals(".")) &&
                (!directory.getName().equals("src/test")))
        {
            File[] contents = directory.listFiles();

            for (File f : contents) {
                if (f.isDirectory()) {
                    removeContents(f);

                    f.delete();
                } else {
                    f.delete();
                }
            }
        }

        if (directory != null)
            directory.delete();
    }

    public static void enlistResources() throws SystemException, RollbackException {
        // resource enlistment is performed via the TransactionManager API
        TransactionManager tm = com.arjuna.ats.jta.TransactionManager.transactionManager();
        // create two resources, although they won't actually do anything, they will force a log record to be created
        DummyXAResource xar1 = new DummyXAResource();
        DummyXAResource xar2 = new DummyXAResource();

        // and enlist them with the transaction
        tm.getTransaction().enlistResource(xar1);
        tm.getTransaction().enlistResource(xar2);
    }
}