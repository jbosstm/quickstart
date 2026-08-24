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
import com.arjuna.ats.internal.arjuna.objectstore.slot.jgroups.JGroupsSlots;
import com.arjuna.ats.internal.arjuna.objectstore.slot.jgroups.JGroupsStoreEnvironmentBean;
import com.arjuna.common.internal.util.propertyservice.BeanPopulator;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Demonstrates disaster recovery using the JGroups SlotStore write-ahead log (WAL).
 *
 * <p>The JGroups ReplCache stores transaction data in memory, replicated across
 * cluster members. When WAL is enabled (the default), each write is also persisted
 * to an Artemis journal on disk. If all cluster members crash and the in-memory
 * cache is lost, the WAL allows each node to recover its transaction data on restart.
 *
 * <p>This example runs in two phases:
 * <ol>
 *   <li><b>create</b> &mdash; start a node, create an in-doubt transaction, then exit
 *       (simulating a crash). The WAL files remain on disk.</li>
 *   <li><b>recover</b> &mdash; restart the node from the same WAL directory. The
 *       SlotStore loads entries from the journal and the recovery store scan finds
 *       the previously in-doubt transaction.</li>
 * </ol>
 *
 * <p>Usage: {@code bash run-jgroups-wal-recovery.sh}
 *
 * <p>Or run manually:
 * <pre>
 *   mvn compile exec:java \
 *     -Dexec.mainClass=org.jboss.narayana.jta.quickstarts.JGroupsSlotStoreWALRecoveryExample \
 *     -Dexec.args="create"
 *
 *   mvn exec:java \
 *     -Dexec.mainClass=org.jboss.narayana.jta.quickstarts.JGroupsSlotStoreWALRecoveryExample \
 *     -Dexec.args="recover"
 * </pre>
 */
public class JGroupsSlotStoreWALRecoveryExample {

    private static final String STORE_DIR = "wal-recovery-store";
    private static final String UID_FILE = "wal-recovery-uid.txt";

    private static JGroupsSlots backingSlots;

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Usage: JGroupsSlotStoreWALRecoveryExample <create|recover>");
            System.exit(1);
        }

        String phase = args[0];
        arjPropertyManager.getCoreEnvironmentBean().setNodeIdentifier("wal-node");

        setupStore();

        switch (phase) {
            case "create":
                createPhase();
                break;
            case "recover":
                recoverPhase();
                break;
            default:
                System.err.println("Unknown phase: " + phase + " (use 'create' or 'recover')");
                System.exit(1);
        }
    }

    private static void createPhase() throws Exception {
        log("=== Phase 1: Create in-doubt transaction ===");

        Uid txnUid = createInDoubtTransaction();
        log("In-doubt transaction created: %s", txnUid);

        // Verify the record is in the store
        RecoveryStore rs = StoreManager.getRecoveryStore();
        AtomicAction probe = new AtomicAction(txnUid);
        InputObjectState record = rs.read_committed(txnUid, probe.type());
        log("Record in store: %s", record != null ? "yes" : "NO");

        // Save the UID so the recover phase can verify it
        Files.writeString(Path.of(UID_FILE), txnUid.toString());

        // List WAL files
        File storeDir = new File(STORE_DIR);
        if (storeDir.isDirectory()) {
            String[] files = storeDir.list();
            log("WAL files in %s/: %s", STORE_DIR,
                    files != null ? String.join(", ", files) : "(empty)");
        }

        shutdown();
        log("Node stopped (simulating crash). WAL files persist on disk.");
    }

    private static void recoverPhase() throws Exception {
        log("=== Phase 2: Recover from WAL ===");

        if (!new File(UID_FILE).exists()) {
            log("FAIL: %s not found. Run the 'create' phase first.", UID_FILE);
            shutdown();
            System.exit(1);
        }

        String expectedUidStr = Files.readString(Path.of(UID_FILE)).trim();
        log("Expecting to recover transaction: %s", expectedUidStr);

        List<Uid> uids = scanRecoveryStore();
        log("Found %d in-doubt transaction(s) via recovery store:", uids.size());
        for (Uid uid : uids) {
            log("  %s%s", uid, uid.toString().equals(expectedUidStr) ? " <-- recovered from WAL" : "");
        }

        boolean found = uids.stream().anyMatch(u -> u.toString().equals(expectedUidStr));
        log(found
                ? "SUCCESS: in-doubt transaction recovered from WAL after total cluster loss"
                : "FAIL: expected transaction %s not found in recovery store", expectedUidStr);

        shutdown();
        System.exit(found ? 0 : 1);
    }

    private static void setupStore() {
        JGroupsStoreEnvironmentBean config = new JGroupsStoreEnvironmentBean();
        config.setExperimentalEnabled(true);
        backingSlots = new JGroupsSlots();

        config.setJGroupsConfigFileName("jgroups-config.xml");
        config.setNodeAddress("wal-node");
        config.setCacheName("walRecoveryCache");
        config.setClusterName("walRecoveryCluster");
        config.setStoreDir(STORE_DIR);
        config.setBackingSlots(backingSlots);
        config.setReplicationCount((short) -1);

        BeanPopulator.getDefaultInstance(ObjectStoreEnvironmentBean.class)
                .setObjectStoreType(SlotStoreAdaptor.class.getName());
        BeanPopulator.setBeanInstanceIfAbsent(
                SlotStoreEnvironmentBean.class.getName(), config);
    }

    private static Uid createInDoubtTransaction() throws Exception {
        AtomicAction aa = new AtomicAction();
        aa.begin();

        aa.add(new ClusterExampleSupport.CrashInCommitRecord());
        aa.add(new ClusterExampleSupport.CrashInCommitRecord());

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
        System.out.printf("[wal-recovery] %s%n", String.format(fmt, args));
    }
}
