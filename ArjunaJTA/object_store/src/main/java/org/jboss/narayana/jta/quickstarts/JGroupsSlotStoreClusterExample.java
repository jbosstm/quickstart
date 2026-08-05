/*
 * Copyright The Narayana Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.narayana.jta.quickstarts;

import com.arjuna.ats.arjuna.common.ObjectStoreEnvironmentBean;
import com.arjuna.ats.internal.arjuna.objectstore.slot.SlotStoreAdaptor;
import com.arjuna.ats.internal.arjuna.objectstore.slot.SlotStoreEnvironmentBean;
import com.arjuna.ats.internal.arjuna.objectstore.slot.jgroups.ByteArrayKey;
import com.arjuna.ats.internal.arjuna.objectstore.slot.jgroups.JGroupsSlots;
import com.arjuna.ats.internal.arjuna.objectstore.slot.jgroups.JGroupsStoreEnvironmentBean;
import com.arjuna.common.internal.util.propertyservice.BeanPopulator;
import org.jgroups.blocks.ReplCache;

import java.util.concurrent.TimeUnit;

/**
 * Demonstrates a cluster of transaction managers sharing a JGroups
 * ReplCache-backed object store. When a node has in-doubt transactions,
 * other cluster members can see them via the recovery store.
 *
 * <p>Run two nodes in separate processes:
 * <pre>
 *   Terminal 1: mvn compile exec:java \
 *     -Dexec.mainClass=org.jboss.narayana.jta.quickstarts.JGroupsSlotStoreClusterExample \
 *     -Dexec.args="node1" -Djgroups.bind_port=7800
 *
 *   Terminal 2: mvn compile exec:java \
 *     -Dexec.mainClass=org.jboss.narayana.jta.quickstarts.JGroupsSlotStoreClusterExample \
 *     -Dexec.args="node2" -Djgroups.bind_port=7801
 * </pre>
 *
 * <p>Or use the provided script: {@code bash run-jgroups-cluster.sh}
 *
 * <p>Node1 creates an in-doubt transaction (commit fails on a participant)
 * and keeps the cluster alive. Node2 starts, joins the cluster, picks up
 * node1's replicated data, creates its own in-doubt transaction, then
 * scans the recovery store and verifies both transactions are visible.
 */
public class JGroupsSlotStoreClusterExample {

    private static JGroupsSlots backingSlots;

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Usage: JGroupsSlotStoreClusterExample <nodeId>");
            System.err.println("  Set -Djgroups.bind_port=<port> per node (default 7800)");
            System.exit(1);
        }

        ClusterExampleSupport support = new ClusterExampleSupport(args[0]);
        support.log("Starting cluster node");

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

    private static void setupStore(ClusterExampleSupport support) throws Exception {
        JGroupsStoreEnvironmentBean config = new JGroupsStoreEnvironmentBean();
        config.setExperimentalEnabled(true);
        backingSlots = new JGroupsSlots();

        config.setJGroupsConfigFileName("jgroups-tcp-config.xml");
        config.setNodeAddress(support.nodeId());
        config.setCacheName("clusterTxStore");
        config.setClusterName("clusterTxStore");
        config.setStoreDir("SlotStore-" + support.nodeId());
        config.setBackingSlots(backingSlots);
        config.setReplicationCount((short) -1);

        if (!support.isNode1()) {
            // ReplCache rebalance() runs asynchronously ~100ms after the
            // view change and sends entries via synchronous RPCs. Pre-start
            // the cache and poll the L2 cache size until migrated entries
            // arrive and stabilize, so cache.stop() cannot interrupt a
            // transfer still in progress. The subsequent JGroupsSlots.init()
            // -> cache.start() then sees the migrated keys in the L2 cache
            // (stop() preserves in-memory L2 data).
            ReplCache<ByteArrayKey, byte[]> cache = config.getCache();
            cache.start();

            int entryCount = 0;
            int stableChecks = 0;
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);

            while (System.nanoTime() < deadline) {
                Thread.sleep(200);
                int size = cache.getL2Cache().getSize();
                if (size > 0 && size == entryCount) {
                    if (++stableChecks >= 3) {
                        break;
                    }
                } else {
                    stableChecks = 0;
                    entryCount = size;
                }
            }

            cache.stop();

            if (stableChecks >= 3) {
                support.log("State transfer complete: %d cache entries", entryCount);
            } else {
                support.log("WARN: state transfer timed out after 10 s (%d cache entries)", entryCount);
            }
        }

        BeanPopulator.getDefaultInstance(ObjectStoreEnvironmentBean.class)
                .setObjectStoreType(SlotStoreAdaptor.class.getName());
        BeanPopulator.setBeanInstanceIfAbsent(
                SlotStoreEnvironmentBean.class.getName(), config);
    }
}
