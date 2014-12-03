package org.jboss.narayana.jta.quickstarts;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public interface HayksJTSHelper {

    /**
     * 1. Read the xids of pending transactions. To check the count of pending transactions.
     *
     * Obtain a map of global transaction id to the wrapper that encapsulates a pending JTS transaction.
     *
     * To get the xid use {@link ArjunaTransactionWrapper#getXid()} on the return values
     * To get the {@link com.arjuna.ats.arjuna.coordinator.AbstractRecord#type()} of the
     * txn use {@link ArjunaTransactionWrapper#getType()} on the returned values
     *
     * @param txns a map to receive all the pending JTS transactions in the current store
     */
    void getPendingJTSTxns(Map<String, ArjunaTransactionWrapper> txns);

    /**
     * 2. Read participant ids of a pending transaction. To check the count of participants.
     * Find JTS transaction participants
     *
     * @param txn a JTS transaction as returned by {@link HayksJTSHelper#getPendingJTSTxns(java.util.Map)}
     * @return the xids of participants in txn that may be pending
     */
    Collection<XidWrapper> getParticipants(ArjunaTransactionWrapper txn);

    /**
     * 3. Read participant resources. To check participant status.
     *
     * Use {@link XidWrapper#getHeuristicValue()} to get the heuristic status of the participant. The
     * meaning of the integer value is defined by constants
     * in {@link com.arjuna.ats.arjuna.coordinator.TwoPhaseOutcome}
     *
     * @param xids a collection of xid wrappers for receiving all pending JTS transaction XA participants
     */
    public void getPendingXids(Collection<XidWrapper> xids);

    /**
     * @return the record types in the current object store
     * @throws Exception
     */
    Set<String> getTypes() throws Exception;
}