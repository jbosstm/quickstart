/*
 * Copyright The Narayana Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.narayana.jta.quickstarts;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.common.ObjectStoreEnvironmentBean;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.common.arjPropertyManager;
import com.arjuna.ats.arjuna.coordinator.TransactionReaper;
import com.arjuna.ats.arjuna.objectstore.RecoveryStore;
import com.arjuna.ats.arjuna.objectstore.StateStatus;
import com.arjuna.ats.arjuna.objectstore.StoreManager;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.internal.arjuna.common.UidHelper;
import com.arjuna.ats.internal.arjuna.objectstore.slot.SlotStoreAdaptor;
import com.arjuna.ats.internal.arjuna.objectstore.slot.SlotStoreEnvironmentBean;
import com.arjuna.ats.internal.arjuna.objectstore.slot.jgroups.JGroupsRaftSlots;
import com.arjuna.ats.internal.arjuna.objectstore.slot.jgroups.JGroupsRaftStoreEnvironmentBean;
import com.arjuna.common.internal.util.propertyservice.BeanPopulator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Demonstrates a cluster of transaction managers sharing a JGroups
 * Raft-backed object store. Writes go through Raft consensus for
 * strong consistency, and data is persisted via the Raft log.
 *
 * <p>Node1 bootstraps a single-member Raft cluster, creates an in-doubt
 * transaction, then waits. Node2 joins the cluster dynamically via
 * the REDIRECT protocol, receives node1's data through Raft log replay,
 * creates its own in-doubt transaction, then scans the recovery store
 * and verifies both transactions are visible.
 *
 * <p>Unlike the ReplCache-based cluster example, Raft uses integer slot
 * IDs directly (shared across all nodes) and log replay is synchronous
 * during connect(), so no pre-start polling is needed.
 *
 * <p>Run two nodes in separate processes:
 * <pre>
 *   Terminal 1: mvn compile exec:java \
 *     -Dexec.mainClass=org.jboss.narayana.jta.quickstarts.JGroupsRaftSlotStoreClusterExample \
 *     -Dexec.args="node1" -Djgroups.bind_port=7800
 *
 *   Terminal 2: mvn compile exec:java \
 *     -Dexec.mainClass=org.jboss.narayana.jta.quickstarts.JGroupsRaftSlotStoreClusterExample \
 *     -Dexec.args="node2" -Djgroups.bind_port=7801
 * </pre>
 *
 * <p>Or use the provided script: {@code bash run-jgroups-raft-cluster.sh}
 */
public class JGroupsRaftSlotStoreClusterExample {

    private static JGroupsRaftSlots backingSlots;
    private static String nodeId;

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Usage: JGroupsRaftSlotStoreClusterExample <nodeId>");
            System.err.println("  Set -Djgroups.bind_port=<port> per node (default 7800)");
            System.exit(1);
        }

        nodeId = args[0];
        log("Starting Raft cluster node");

        arjPropertyManager.getCoreEnvironmentBean().setNodeIdentifier(nodeId);

        if (!"node1".equals(nodeId)) {
            log("Waiting for node1 to be ready...");
            while (!new File("node1.ready").exists()) {
                Thread.sleep(200);
            }
            Thread.sleep(1000);
        }

        setupStore();

        log("Creating in-doubt transaction...");
        Uid txnUid = createInDoubtTransaction();
        log("In-doubt transaction created: %s", txnUid);

        File marker = new File(nodeId + ".ready");
        marker.createNewFile();
        marker.deleteOnExit();

        if ("node1".equals(nodeId)) {
            log("Keeping Raft cluster alive for other nodes (Ctrl-C to stop)");
            try {
                Thread.sleep(Long.MAX_VALUE);
            } catch (InterruptedException ignored) {
            }
        } else {
            List<Uid> uids = scanRecoveryStore();
            log("Found %d in-doubt transaction(s) via recovery store:", uids.size());
            for (Uid uid : uids) {
                log("  %s", uid);
            }

            boolean success = uids.size() >= 2;
            log(success
                    ? "SUCCESS: transactions from other nodes are visible for recovery"
                    : "FAIL: expected to see transactions from other nodes (found %d)", uids.size());

            shutdown();
            System.exit(success ? 0 : 1);
        }
    }

    private static void setupStore() {
        JGroupsRaftStoreEnvironmentBean config = new JGroupsRaftStoreEnvironmentBean();
        backingSlots = new JGroupsRaftSlots();

        config.setJGroupsConfigFileName("jgroups-raft-tcp-config.xml");
        config.setNodeAddress(nodeId);
        config.setCacheName("raftClusterTxStore");
        config.setClusterName("raftClusterTxStore");
        config.setStoreDir("RaftStore-" + nodeId);
        config.setBackingSlots(backingSlots);

        if ("node1".equals(nodeId)) {
            // Node1 bootstraps a single-member cluster
            config.setRaftMembers("node1");
        }
        // Other nodes join dynamically (no raftMembers set)

        BeanPopulator.getDefaultInstance(ObjectStoreEnvironmentBean.class)
                .setObjectStoreType(SlotStoreAdaptor.class.getName());
        BeanPopulator.setBeanInstanceIfAbsent(
                SlotStoreEnvironmentBean.class.getName(), config);
    }

    private static Uid createInDoubtTransaction() throws Exception {
        AtomicAction aa = new AtomicAction();
        aa.begin();

        aa.add(new JGroupsSlotStoreClusterExample.CrashInCommitRecord());
        aa.add(new JGroupsSlotStoreClusterExample.CrashInCommitRecord());

        aa.commit(true);

        return aa.getSavingUid();
    }

    private static List<Uid> scanRecoveryStore() {
        List<Uid> uids = new ArrayList<>();
        RecoveryStore rs = StoreManager.getRecoveryStore();
        AtomicAction probe = new AtomicAction();
        InputObjectState ios = new InputObjectState();

        try {
            if (rs.allObjUids(probe.type(), ios, StateStatus.OS_UNKNOWN)) {
                Uid uid;
                do {
                    uid = UidHelper.unpackFrom(ios);
                    if (!uid.equals(Uid.nullUid())) {
                        uids.add(uid);
                    }
                } while (!uid.equals(Uid.nullUid()));
            }
        } catch (Exception e) {
            log("Error scanning recovery store: %s", e.getMessage());
        }

        return uids;
    }

    private static void shutdown() {
        if (backingSlots != null) {
            backingSlots.stop();
        }
        TransactionReaper.terminate(true);
    }

    private static void log(String fmt, Object... args) {
        System.out.printf("[%s] %s%n", nodeId, String.format(fmt, args));
    }
}
