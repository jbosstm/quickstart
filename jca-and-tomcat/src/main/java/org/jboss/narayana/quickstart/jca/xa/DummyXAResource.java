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
package org.jboss.narayana.quickstart.jca.xa;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.jboss.logging.Logger;

/**
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 *
 */
public final class DummyXAResource implements XAResource, Serializable {

    private static final long serialVersionUID = 8762654005306824997L;

    private static final Logger LOG = Logger.getLogger(DummyXAResource.class);

    private static final AtomicInteger counter = new AtomicInteger();

    /**
     * Returns number of successfully commited transactions.
     *
     * @return Commited transactions counter.
     */
    public int getCommitedTransactionsCounter() {
        if (LOG.isTraceEnabled()) {
            LOG.trace("DummyXAResource.getCommitedTransactionsCounter()");
        }

        return counter.get();
    }

    public void commit(Xid xid, boolean onePhase) throws XAException {
        LOG.info("DummyXAResource.commit(xid=" + xid + ", onePhase=" + onePhase + ")");

        counter.incrementAndGet();
    }

    public void end(Xid xid, int flags) throws XAException {
        if (LOG.isTraceEnabled()) {
            LOG.trace("DummyXAResource.end(xid=" + xid + ", flags=" + flags + ")");
        }
    }

    public void forget(Xid xid) throws XAException {
        if (LOG.isTraceEnabled()) {
            LOG.trace("DummyXAResource.forget(xid=" + xid + ")");
        }
    }

    public int getTransactionTimeout() throws XAException {
        if (LOG.isTraceEnabled()) {
            LOG.trace("DummyXAResource.getTransactionTimeout()");
        }

        return 0;
    }

    public boolean isSameRM(XAResource xares) throws XAException {
        if (LOG.isTraceEnabled()) {
            LOG.trace("DummyXAResource.isSameRM(xares=" + xares + ")");
        }

        return false;
    }

    public int prepare(Xid xid) throws XAException {
        LOG.info("DummyXAResource.prepare(xid=" + xid + ")");

        return 0;
    }

    public Xid[] recover(int flag) throws XAException {
        LOG.info("DummyXAResource.recover(flag=" + flag + ")");

        return null;
    }

    public void rollback(Xid xid) throws XAException {
        LOG.info("DummyXAResource.rollback(xid=" + xid + ")");
    }

    public boolean setTransactionTimeout(int seconds) throws XAException {
        if (LOG.isTraceEnabled()) {
            LOG.trace("DummyXAResource.setTransactionTimeout(seconds=" + seconds + ")");
        }

        return false;
    }

    public void start(Xid xid, int flags) throws XAException {
        if (LOG.isTraceEnabled()) {
            LOG.trace("DummyXAResource.start(xid=" + xid + ", flags=" + flags + ")");
        }
    }

}
