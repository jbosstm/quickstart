package org.jboss.narayana.quickstart.hibernate;

import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public final class DummyXAResourceManager {

    private static final Logger LOG = Logger.getLogger(DummyXAResourceManager.class);

    /**
     * Returns the number of successfully committed transactions.
     *
     * @return transactions counter.
     */
    public int getCommitedTransactionsCounter() {
        final DummyXAResource dummyXAResource = new DummyXAResource();

        return dummyXAResource.getCommitedTransactionsCounter();
    }

}