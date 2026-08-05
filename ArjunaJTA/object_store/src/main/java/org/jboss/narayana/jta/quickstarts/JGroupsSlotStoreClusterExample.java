/*
 * Copyright The Narayana Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.narayana.jta.quickstarts;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.common.ObjectStoreEnvironmentBean;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.common.arjPropertyManager;
import com.arjuna.ats.arjuna.coordinator.AbstractRecord;
import com.arjuna.ats.arjuna.coordinator.RecordType;
import com.arjuna.ats.arjuna.coordinator.TwoPhaseOutcome;
import com.arjuna.ats.arjuna.coordinator.TransactionReaper;
import com.arjuna.ats.arjuna.objectstore.RecoveryStore;
import com.arjuna.ats.arjuna.objectstore.StateStatus;
import com.arjuna.ats.arjuna.objectstore.StoreManager;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.internal.arjuna.common.UidHelper;
import com.arjuna.ats.internal.arjuna.objectstore.slot.SlotStoreAdaptor;
import com.arjuna.ats.internal.arjuna.objectstore.slot.SlotStoreEnvironmentBean;
import com.arjuna.ats.internal.arjuna.objectstore.slot.jgroups.ByteArrayKey;
import com.arjuna.ats.internal.arjuna.objectstore.slot.jgroups.JGroupsSlots;
import com.arjuna.ats.internal.arjuna.objectstore.slot.jgroups.JGroupsStoreEnvironmentBean;
import com.arjuna.common.internal.util.propertyservice.BeanPopulator;
import org.jgroups.blocks.ReplCache;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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

    private static JGroupsStoreEnvironmentBean config;
    private static JGroupsSlots backingSlots;
    private static String nodeId;

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Usage: JGroupsSlotStoreClusterExample <nodeId>");
            System.err.println("  Set -Djgroups.bind_port=<port> per node (default 7800)");
            System.exit(1);
        }

        nodeId = args[0];
        log("Starting cluster node");

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

        // notify other nodes that the in-doubt transaction should be available by creating a marker file
        File marker = new File(nodeId + ".ready");
        marker.createNewFile();
        marker.deleteOnExit();

        if ("node1".equals(nodeId)) {
            log("Keeping cluster alive for other nodes (Ctrl-C to stop)");
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

    private static void setupStore() throws Exception {
        config = new JGroupsStoreEnvironmentBean();
        backingSlots = new JGroupsSlots();

        config.setJGroupsConfigFileName("jgroups-tcp-config.xml");
        config.setNodeAddress(nodeId);
        config.setCacheName("clusterTxStore");
        config.setClusterName("clusterTxStore");
        config.setStoreDir("SlotStore-" + nodeId);
        config.setBackingSlots(backingSlots);
        config.setReplicationCount((short) -1);

        if (!"node1".equals(nodeId)) {
            // ReplCache migrateData is asynchronous: replicated entries arrive
            // after cache.start() returns. Pre-start the cache and poll until
            // the entries are present, so JGroupsSlots.load() sees them.
            ReplCache<ByteArrayKey, byte[]> cache = config.getCache();
            cache.start();

            long deadline = System.currentTimeMillis() + 10_000;
            while (cache.getL2Cache().getInternalMap().isEmpty()
                    && System.currentTimeMillis() < deadline) {
                Thread.sleep(200);
            }
            log("State transfer complete: %d cache entries",
                    cache.getL2Cache().getInternalMap().size());
        }

        BeanPopulator.getDefaultInstance(ObjectStoreEnvironmentBean.class)
                .setObjectStoreType(SlotStoreAdaptor.class.getName());
        BeanPopulator.setBeanInstanceIfAbsent(
                SlotStoreEnvironmentBean.class.getName(), config);
    }

    /**
     * Create a transaction with participants that fail during commit,
     * leaving an in-doubt record in the store for recovery.
     * Uses the same pattern as JGroupsRecoveryScanTest in narayana.
     */
    private static Uid createInDoubtTransaction() throws Exception {
        AtomicAction aa = new AtomicAction();
        aa.begin();

        aa.add(new CrashInCommitRecord());
        aa.add(new CrashInCommitRecord());

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

    /**
     * An AbstractRecord that prepares successfully but returns FINISH_ERROR
     * during commit, leaving the transaction in-doubt for recovery.
     * Follows the same pattern as CrashRecord(CrashInCommit, Normal)
     * from the narayana test suite.
     */
    public static class CrashInCommitRecord extends AbstractRecord {
        public CrashInCommitRecord() {
            super();
        }

        @Override
        public int topLevelPrepare() {
            return TwoPhaseOutcome.PREPARE_OK;
        }

        @Override
        public int topLevelCommit() {
            return TwoPhaseOutcome.FINISH_ERROR;
        }

        @Override
        public int topLevelAbort() {
            return TwoPhaseOutcome.FINISH_OK;
        }

        @Override
        public int nestedPrepare() {
            return TwoPhaseOutcome.PREPARE_OK;
        }

        @Override
        public int nestedCommit() {
            return TwoPhaseOutcome.FINISH_OK;
        }

        @Override
        public int nestedAbort() {
            return TwoPhaseOutcome.FINISH_OK;
        }

        @Override
        public boolean shouldAdd(AbstractRecord a) {
            return true;
        }

        @Override
        public boolean shouldAlter(AbstractRecord a) {
            return false;
        }

        @Override
        public boolean shouldMerge(AbstractRecord a) {
            return false;
        }

        @Override
        public boolean shouldReplace(AbstractRecord a) {
            return false;
        }

        @Override
        public int typeIs() {
            return RecordType.USER_DEF_FIRST0;
        }

        @Override
        public Object value() {
            return null;
        }

        @Override
        public void setValue(Object o) {
        }

        @Override
        public void merge(AbstractRecord a) {
        }

        @Override
        public void alter(AbstractRecord a) {
        }

        @Override
        public boolean save_state(OutputObjectState os, int i) {
            return super.save_state(os, i);
        }

        @Override
        public boolean restore_state(InputObjectState os, int i) {
            return super.restore_state(os, i);
        }
    }
}
