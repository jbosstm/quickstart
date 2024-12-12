package org.jboss.narayana.jta.quickstarts;

import java.io.File;

import jakarta.transaction.UserTransaction;

import com.arjuna.ats.arjuna.common.ObjectStoreEnvironmentBean;
import com.arjuna.common.internal.util.propertyservice.BeanPopulator;

public class VolatileStoreExample {
    private static final String storeClassName = com.arjuna.ats.internal.arjuna.objectstore.VolatileStore.class.getName();
    private static String defaultStoreDir;

    public static void main(String[] args) throws Exception {
        setupStore();
        UserTransaction utx = com.arjuna.ats.jta.UserTransaction.userTransaction();

        utx.begin();
        Util.enlistResources();
        utx.commit();

        if (new File(defaultStoreDir).exists())
            throw new RuntimeException(defaultStoreDir + ": store directory should not have been created");
    }

    public static void setupStore() throws Exception {
        defaultStoreDir = BeanPopulator.getDefaultInstance(ObjectStoreEnvironmentBean.class).getObjectStoreDir();

        BeanPopulator.getDefaultInstance(ObjectStoreEnvironmentBean.class).setObjectStoreType(storeClassName);
        BeanPopulator.getNamedInstance(ObjectStoreEnvironmentBean.class, "default").setObjectStoreType(storeClassName);
        BeanPopulator.getNamedInstance(ObjectStoreEnvironmentBean.class, "communicationStore").setObjectStoreType(storeClassName);
        Util.emptyObjectStore();
    }
}