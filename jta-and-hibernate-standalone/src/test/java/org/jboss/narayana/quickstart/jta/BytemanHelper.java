package org.jboss.narayana.quickstart.jta;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.ActionManager;
import com.arjuna.ats.internal.arjuna.thread.ThreadActionData;

/**
 * A Byteman helper class used to simulate crash during the first commit.
 * Helper has a commits counter, to make sure crash is simulated only for the first commit.
 * Crash is simulated by throwing an exception during the commit execution, thus not completing it, removing
 * transaction's uid from the action manager, and removing transaction from the thread.
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class BytemanHelper {

    private static int commitsCounter;

    public static void reset() {
        commitsCounter = 0;
    }

    public void failFirstCommit(Uid uid) {
        // Increment is called first, so counter should be 1
        if (commitsCounter == 1) {
            System.out.println(BytemanHelper.class.getName() + " fail first commit");
            ActionManager.manager().remove(uid);
            ThreadActionData.popAction();
            throw new QuickstartRuntimeException("Failing first commit");
        }
    }

    public void incrementCommitsCounter() {
        commitsCounter++;
        System.out.println(BytemanHelper.class.getName() + " increment commits counter: " + commitsCounter);
    }

}