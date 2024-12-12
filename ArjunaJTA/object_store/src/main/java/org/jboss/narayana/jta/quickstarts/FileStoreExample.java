package org.jboss.narayana.jta.quickstarts;

import java.io.File;

import jakarta.transaction.UserTransaction;

import com.arjuna.ats.arjuna.common.ObjectStoreEnvironmentBean;
import com.arjuna.common.internal.util.propertyservice.BeanPopulator;

public class FileStoreExample {
    private static String storeDir = "target/TxStoreDir";

     public static void main(String[] args) throws Exception {
        setupStore();

        UserTransaction utx = com.arjuna.ats.jta.UserTransaction.userTransaction();

        utx.begin();
        Util.enlistResources();
        utx.commit();

        if (!new File(storeDir).exists())
            throw new RuntimeException(storeDir + " should have been created");
    }

    public static void setupStore() throws Exception {
        BeanPopulator.getDefaultInstance(ObjectStoreEnvironmentBean.class).setObjectStoreDir(storeDir);
        BeanPopulator.getNamedInstance(ObjectStoreEnvironmentBean.class, "communicationStore").setObjectStoreDir(storeDir);
    }
}