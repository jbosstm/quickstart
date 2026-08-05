/*
 * Copyright The Narayana Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.narayana.jta.quickstarts;

import com.arjuna.ats.arjuna.common.ObjectStoreEnvironmentBean;
import com.arjuna.ats.arjuna.coordinator.TransactionReaper;
import com.arjuna.ats.internal.arjuna.objectstore.slot.SlotStoreAdaptor;
import com.arjuna.ats.internal.arjuna.objectstore.slot.SlotStoreEnvironmentBean;
import com.arjuna.ats.internal.arjuna.objectstore.slot.jgroups.JGroupsRaftSlots;
import com.arjuna.ats.internal.arjuna.objectstore.slot.jgroups.JGroupsRaftStoreEnvironmentBean;
import com.arjuna.common.internal.util.propertyservice.BeanPopulator;
import jakarta.transaction.UserTransaction;


/**
 * Example showing how to configure a JGroups Raft-backed object store.
 * Writes go through Raft consensus for strong consistency, and a persistent
 * write-ahead log provides crash recovery without a separate WAL layer.
 * <p>
 * This example bootstraps a single-node Raft cluster. For multi-node clusters,
 * set {@code config.setRaftMembers("node1,node2,node3")} with an odd number of
 * members (minimum 3 for fault tolerance).
 */
public class JGroupsRaftSlotStoreExample {

    private final static String CACHE_CONFIG_FILE = "jgroups-raft-config.xml";

    private static JGroupsRaftSlots slots;

    public static void main(String[] args) throws Exception {
        setupStore();
        try {
            UserTransaction utx = com.arjuna.ats.jta.UserTransaction.userTransaction();

            utx.begin();
            Util.enlistResources();
            utx.commit();
        } finally {
            shutdownStore();
        }
    }

    public static void setupStore() {
        JGroupsRaftStoreEnvironmentBean config = new JGroupsRaftStoreEnvironmentBean();
        config.setExperimentalEnabled(true);
        slots = new JGroupsRaftSlots();

        config.setJGroupsConfigFileName(CACHE_CONFIG_FILE);
        config.setNodeAddress("node1");
        config.setRaftMembers("node1");
        config.setStoreDir("raft-log");
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
