/*
 * Copyright The Narayana Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.narayana.jta.quickstarts;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.common.arjPropertyManager;
import com.arjuna.ats.arjuna.coordinator.AbstractRecord;
import com.arjuna.ats.arjuna.coordinator.RecordType;
import com.arjuna.ats.arjuna.coordinator.TransactionReaper;
import com.arjuna.ats.arjuna.coordinator.TwoPhaseOutcome;
import com.arjuna.ats.arjuna.objectstore.RecoveryStore;
import com.arjuna.ats.arjuna.objectstore.StateStatus;
import com.arjuna.ats.arjuna.objectstore.StoreManager;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.internal.arjuna.common.UidHelper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Shared infrastructure for the JGroups and JGroups-Raft cluster quickstart
 * examples. Each example provides only its store configuration; everything
 * else (node coordination, in-doubt transaction creation, recovery scanning,
 * shutdown) lives here.
 */
class ClusterExampleSupport {

    private final String nodeId;

    ClusterExampleSupport(String nodeId) throws Exception {
        this.nodeId = nodeId;
        arjPropertyManager.getCoreEnvironmentBean().setNodeIdentifier(nodeId);
    }

    String nodeId() {
        return nodeId;
    }

    boolean isNode1() {
        return "node1".equals(nodeId);
    }

    void waitForNode1() throws InterruptedException {
        if (!isNode1()) {
            log("Waiting for node1 to be ready...");
            while (!new File("node1.ready").exists()) {
                Thread.sleep(200);
            }
            Thread.sleep(1000);
        }
    }

    void createMarkerFile() throws IOException {
        File marker = new File(nodeId + ".ready");
        marker.createNewFile();
        marker.deleteOnExit();
    }

    Uid createInDoubtTransaction() throws Exception {
        log("Creating in-doubt transaction...");
        AtomicAction aa = new AtomicAction();
        aa.begin();

        aa.add(new CrashInCommitRecord());
        aa.add(new CrashInCommitRecord());

        aa.commit(true);

        Uid uid = aa.getSavingUid();
        log("In-doubt transaction created: %s", uid);
        return uid;
    }

    List<Uid> scanRecoveryStore() {
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

    void verifyRecoveryAndExit(Runnable cleanup) {
        List<Uid> uids = scanRecoveryStore();
        log("Found %d in-doubt transaction(s) via recovery store:", uids.size());
        for (Uid uid : uids) {
            log("  %s", uid);
        }

        boolean success = uids.size() >= 2;
        log(success
                ? "SUCCESS: transactions from other nodes are visible for recovery"
                : "FAIL: expected to see transactions from other nodes (found %d)", uids.size());

        shutdown(cleanup);
        System.exit(success ? 0 : 1);
    }

    void keepAlive() {
        log("Keeping cluster alive for other nodes (Ctrl-C to stop)");
        try {
            Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException ignored) {
        }
    }

    void shutdown(Runnable cleanup) {
        if (cleanup != null) {
            cleanup.run();
        }
        TransactionReaper.terminate(true);
    }

    void log(String fmt, Object... args) {
        System.out.printf("[%s] %s%n", nodeId, String.format(fmt, args));
    }

    /**
     * An AbstractRecord that prepares successfully but returns FINISH_ERROR
     * during commit, leaving the transaction in-doubt for recovery.
     * Follows the same pattern as CrashRecord(CrashInCommit, Normal)
     * from the narayana test suite.
     */
    static class CrashInCommitRecord extends AbstractRecord {
        CrashInCommitRecord() {
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
