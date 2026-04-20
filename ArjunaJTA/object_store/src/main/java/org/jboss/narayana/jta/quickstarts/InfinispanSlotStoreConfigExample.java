package org.jboss.narayana.jta.quickstarts;

import jakarta.transaction.UserTransaction;

import java.io.IOException;

public class InfinispanSlotStoreConfigExample {

    public static void main(String[] args) throws Exception {
        setupStore();

        UserTransaction utx = com.arjuna.ats.jta.UserTransaction.userTransaction();

        utx.begin();
        Util.enlistResources();
        utx.commit();

        System.exit(0); // exit immediately
    }

    public static void setupStore() throws IOException {
        // expose the jbossts properties file containing the config for an infinispan based slot store
        String infinispanConfigFile = "infinispan-jbossts-properties.xml";

        System.setProperty("com.arjuna.ats.arjuna.common.propertiesFile", infinispanConfigFile);
        System.setProperty("nodeName", "node1"); // infinispan-jbossts-properties.xml "reads" the nodeName
    }
}