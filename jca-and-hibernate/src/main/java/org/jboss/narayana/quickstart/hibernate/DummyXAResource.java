/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.narayana.quickstart.hibernate;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.jboss.logging.Logger;

/**
 * Only purpose of this resource is to have second participant in the transaction. This allows us to see the two phase
 * commit protocol being executed.
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public final class DummyXAResource implements XAResource, Serializable {

    private static final long serialVersionUID = 8762654005306824997L;

    private static final Logger LOG = Logger.getLogger(DummyXAResource.class);

    private static final AtomicInteger counter = new AtomicInteger();

    /**
     * Returns number of successfully committed transactions.
     *
     * @return Number of committed transactions.
     */
    public int getCommitedTransactionsCounter() {
        if (LOG.isTraceEnabled()) {
            LOG.trace("DummyXAResource - returning counter of successfully committed transactions: " + counter.get());
        }

        return counter.get();
    }

    public void commit(Xid xid, boolean onePhase) throws XAException {
        if (LOG.isTraceEnabled()) {
            LOG.trace("DummyXAResource - commit invoked");
        }

        counter.incrementAndGet();
    }

    public void end(Xid xid, int flags) throws XAException {
        if (LOG.isTraceEnabled()) {
            LOG.trace("DummyXAResource - end invoked");
        }
    }

    public void forget(Xid xid) throws XAException {
        if (LOG.isTraceEnabled()) {
            LOG.trace("DummyXAResource - forget invoked");
        }
    }

    public int getTransactionTimeout() throws XAException {
        if (LOG.isTraceEnabled()) {
            LOG.trace("DummyXAResource - returning transaction timeout: 0");
        }

        return 0;
    }

    public boolean isSameRM(XAResource xares) throws XAException {
        if (LOG.isTraceEnabled()) {
            LOG.trace("DummyXAResource - isSameRM invoked");
        }

        return false;
    }

    public int prepare(Xid xid) throws XAException {
        if (LOG.isTraceEnabled()) {
            LOG.trace("DummyXAResource - prepare invoked");
        }

        return 0;
    }

    public Xid[] recover(int flag) throws XAException {
        if (LOG.isTraceEnabled()) {
            LOG.trace("DummyXAResource - recover invoked");
        }

        return null;
    }

    public void rollback(Xid xid) throws XAException {
        if (LOG.isTraceEnabled()) {
            LOG.trace("DummyXAResource - rollback invoked");
        }
    }

    public boolean setTransactionTimeout(int seconds) throws XAException {
        if (LOG.isTraceEnabled()) {
            LOG.trace("DummyXAResource - setTransactionTimeout invoked");
        }

        return false;
    }

    public void start(Xid xid, int flags) throws XAException {
        if (LOG.isTraceEnabled()) {
            LOG.trace("DummyXAResource - start invoked");
        }
    }

}
