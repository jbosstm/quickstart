package org.jboss.narayana.jta.quickstarts;

import com.arjuna.ats.arjuna.common.Uid;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public interface HayksJTSHelper {

    /**
     * 1. Read the xids of pending transactions. To check the count of pending transactions.
     *
     * Obtain a map of transaction Uid to the wrapper that encapsulates it.
     *
     * If a transaction has participants then the global transaction id is available by calling
     * the method {@link ArjunaTransactionWrapper#getGtid()}
     *
     * Use  {@link ArjunaTransactionWrapper#getUid()} to get the unique Uid for the transaction
     *
     * To get the {@link com.arjuna.ats.arjuna.coordinator.AbstractRecord#type()} of the
     * txn use {@link ArjunaTransactionWrapper#getType()} on the returned values
     *
     * @return txns a map pending JTS transactions in the current store
     */
    Map<Uid, ArjunaTransactionWrapper> getPendingJTSTxns();

    /**
     * 2. Read participant ids of a pending transaction. To check the count of participants.
     * Find JTS transaction participants
     *
     * @param txn a JTS transaction as returned by {@link HayksJTSHelper#getPendingJTSTxns()}
     * @return the xids of participants in txn that may be pending
     */
    Collection<JTSXAResourceRecordWrapper> getParticipants(ArjunaTransactionWrapper txn);

    /**
     * 3. Read participant resources. To check participant status.
     *
     * Use {@link org.jboss.narayana.jta.quickstarts.JTSXAResourceRecordWrapper#getHeuristicValue()}
     * to get the heuristic status of the participant. The meaning of the integer value is defined by constants
     * in {@link com.arjuna.ats.arjuna.coordinator.TwoPhaseOutcome}
     *
     * @return a collection of xid wrappers for receiving all pending JTS transaction XA participants
     */
    Collection<JTSXAResourceRecordWrapper> getPendingXids();

    /**
     * @return the record types in the current object store
     * @throws Exception
     */
    Set<String> getTypes() throws Exception;

    /**
     * Convert bytes represent a component of an Xid into a human readable string
     * @param bytes
     * @return
     */
    StringBuilder convertXidBytes(byte[] bytes);
}