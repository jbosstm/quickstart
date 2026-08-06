/*
 * Copyright The Narayana Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.narayana.jta.quickstarts;

import com.arjuna.ats.arjuna.common.ObjectStoreEnvironmentBean;
import com.arjuna.ats.internal.arjuna.objectstore.slot.SlotStoreAdaptor;
import com.arjuna.ats.internal.arjuna.objectstore.slot.SlotStoreEnvironmentBean;
import com.arjuna.ats.internal.arjuna.objectstore.slot.jgroups.JGroupsRaftSlots;
import com.arjuna.ats.internal.arjuna.objectstore.slot.jgroups.JGroupsRaftStoreEnvironmentBean;
import com.arjuna.common.internal.util.propertyservice.BeanPopulator;

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

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Usage: JGroupsRaftSlotStoreClusterExample <nodeId>");
            System.err.println("  Set -Djgroups.bind_port=<port> per node (default 7800)");
            System.exit(1);
        }

        ClusterExampleSupport support = new ClusterExampleSupport(args[0]);
        support.log("Starting Raft cluster node");

        support.waitForNode1();
        setupStore(support);
        support.createInDoubtTransaction();
        support.createMarkerFile();

        if (support.isNode1()) {
            support.keepAlive();
        } else {
            support.verifyRecoveryAndExit(() -> { if (backingSlots != null) backingSlots.stop(); });
        }
    }

    private static void setupStore(ClusterExampleSupport support) {
        JGroupsRaftStoreEnvironmentBean config = new JGroupsRaftStoreEnvironmentBean();
        backingSlots = new JGroupsRaftSlots();

        config.setJGroupsConfigFileName("jgroups-raft-tcp-config.xml");
        config.setNodeAddress(support.nodeId());
        config.setCacheName("raftClusterTxStore");
        config.setClusterName("raftClusterTxStore");
        config.setStoreDir("RaftStore-" + support.nodeId());
        config.setBackingSlots(backingSlots);

        if (support.isNode1()) {
            // Node1 bootstraps a single-member cluster
            config.setRaftMembers("node1");
        }
        // Other nodes join dynamically (no raftMembers set)

        BeanPopulator.getDefaultInstance(ObjectStoreEnvironmentBean.class)
                .setObjectStoreType(SlotStoreAdaptor.class.getName());
        BeanPopulator.setBeanInstanceIfAbsent(
                SlotStoreEnvironmentBean.class.getName(), config);
    }
}
