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
package org.jboss.narayana.quickstart.jca.controller;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;

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
