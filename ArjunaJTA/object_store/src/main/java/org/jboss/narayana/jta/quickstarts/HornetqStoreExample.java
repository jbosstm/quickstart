package org.jboss.narayana.jta.quickstarts;

import java.io.File;

import jakarta.transaction.UserTransaction;

import com.arjuna.ats.arjuna.common.ObjectStoreEnvironmentBean;
import com.arjuna.ats.arjuna.objectstore.StoreManager;
import com.arjuna.ats.internal.arjuna.objectstore.hornetq.HornetqJournalEnvironmentBean;
import com.arjuna.common.internal.util.propertyservice.BeanPopulator;

public class HornetqStoreExample {
    private static final String storeClassName =  com.arjuna.ats.internal.arjuna.objectstore.hornetq.HornetqObjectStoreAdaptor.class.getName();
    private static final String storeDir = "target/HornetqStore";

    public static void main(String[] args) throws Exception {
        setupStore();
        UserTransaction utx = com.arjuna.ats.jta.UserTransaction.userTransaction();

        utx.begin();
        utx.commit();

		shutdownStore();
    }

    public static void setupStore() throws Exception {
        File hornetqStoreDir = new File(storeDir);

        BeanPopulator.getDefaultInstance(HornetqJournalEnvironmentBean.class)
                    .setStoreDir(hornetqStoreDir.getCanonicalPath());

        BeanPopulator.getDefaultInstance(ObjectStoreEnvironmentBean.class).setObjectStoreType(storeClassName);
        BeanPopulator.getNamedInstance(ObjectStoreEnvironmentBean.class, "communicationStore").setObjectStoreDir(storeDir);

        BeanPopulator.getNamedInstance(ObjectStoreEnvironmentBean.class, "default").setObjectStoreType(storeClassName);
        BeanPopulator.getNamedInstance(ObjectStoreEnvironmentBean.class, "communicationStore").setObjectStoreType(storeClassName);
    }

    public static void shutdownStore() throws Exception {
        StoreManager.shutdown();

        if (!new File(storeDir).exists())
            throw new RuntimeException(storeDir + " should have been created");
	}
}