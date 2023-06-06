package org.jboss.narayana.quickstart.jca.controller;

import jakarta.faces.bean.ManagedBean;
import jakarta.faces.bean.RequestScoped;

import org.jboss.logging.Logger;
import org.jboss.narayana.quickstart.jca.xa.DummyXAResource;

/**
 * Bean used to manage dummy XA resource.
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 *
 */
@ManagedBean
@RequestScoped
public final class DummyXAResourceManager {

    private static final Logger LOG = Logger.getLogger(DummyXAResourceManager.class);

    /**
     * Returns the number of successfully commited transactions.
     *
     * @return transactions counter.
     */
    public int getCommitedTransactionsCounter() {
        if (LOG.isTraceEnabled()) {
            LOG.trace("DummyXAResourceManager.getCommitedTransactionsCounter()");
        }

        final DummyXAResource dummyXAResource = new DummyXAResource();

        return dummyXAResource.getCommitedTransactionsCounter();
    }

}