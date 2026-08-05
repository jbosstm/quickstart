/*
 * Copyright The Narayana Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.narayana.jta.quickstarts;

import com.arjuna.ats.arjuna.common.ObjectStoreEnvironmentBean;
import com.arjuna.ats.arjuna.coordinator.TransactionReaper;
import com.arjuna.ats.internal.arjuna.objectstore.slot.SlotStoreAdaptor;
import com.arjuna.ats.internal.arjuna.objectstore.slot.SlotStoreEnvironmentBean;
import com.arjuna.ats.internal.arjuna.objectstore.slot.jgroups.JGroupsSlots;
import com.arjuna.ats.internal.arjuna.objectstore.slot.jgroups.JGroupsStoreEnvironmentBean;
import com.arjuna.common.internal.util.propertyservice.BeanPopulator;
import jakarta.transaction.UserTransaction;

/**
 * Example showing how to configure a JGroups ReplCache-backed object store.
 * Data is replicated in-memory across the JGroups cluster with an optional
 * write-ahead log (WAL) for crash recovery.
 */
public class JGroupsSlotStoreExample {

    private final static String CACHE_CONFIG_FILE = "jgroups-config.xml";
    private static JGroupsSlots slots;

    public static void main(String[] args) throws Exception {
        setupStore();

        UserTransaction utx = com.arjuna.ats.jta.UserTransaction.userTransaction();

        utx.begin();
        Util.enlistResources();
        utx.commit();

        shutdownStore();

        System.exit(0);
    }

    public static void setupStore() {
        JGroupsStoreEnvironmentBean config = new JGroupsStoreEnvironmentBean();
        slots = new JGroupsSlots();

        config.setJGroupsConfigFileName(CACHE_CONFIG_FILE);
        config.setNodeAddress("node1");
        config.setCacheName("replCache");
        config.setBackingSlots(slots);

        BeanPopulator.getDefaultInstance(ObjectStoreEnvironmentBean.class)
                .setObjectStoreType(SlotStoreAdaptor.class.getName());
        // Register the bean under the right key so that SlotStore.java reads the right one
        BeanPopulator.setBeanInstanceIfAbsent(SlotStoreEnvironmentBean.class.getName(), config);
    }

    public static void shutdownStore() {
        if (slots != null) {
            slots.stop();
        }

        TransactionReaper.terminate(true);
    }
}
