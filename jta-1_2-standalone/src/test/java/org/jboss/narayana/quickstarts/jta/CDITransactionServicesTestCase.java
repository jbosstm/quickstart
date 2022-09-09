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
package org.jboss.narayana.quickstarts.jta;

import jakarta.transaction.Status;
import jakarta.transaction.SystemException;
import jakarta.transaction.TransactionManager;
import jakarta.transaction.TransactionalException;

import org.jboss.narayana.quickstarts.jta.cdi.CDITransactionServices;
import org.jboss.narayana.quickstarts.jta.cdi.CDITransactionsProducers;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.jboss.weld.transaction.spi.TransactionServices;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * <p>
 * This test case shows how to initiate the Narayana transaction manager
 * when used with the CDI standalone container.
 * </p>
 * <p>
 * <ul>
 * <li>The Transaction Manager is taken looked up by the {@link TransactionalException}
 * in JNDI and as it's not found it falls back to the Narayana implementation one.
 * See {@link com.arjuna.ats.jta.TransactionManager#transactionManager()}.</li>
 * <li>The synchronization registry is created as the CDI bean
 * by the producer at {@link CDITransactionsProducers}.</li>
 * </ul>
 * </p>
 */
public class CDITransactionServicesTestCase {

    private Weld weld;
    private TransactionManager transactionManager;

    private RequiredCounterManager requiredManager;
    private Counter counter;
    private EventsCounter lifeCycleCounter;

    @Before
    public void before() throws Exception {
        // Initialize Weld container
        weld = new Weld()
            .addServices(new CDITransactionServices());

        final WeldContainer weldContainer = weld.initialize();

        counter = weldContainer.select(Counter.class).get();
        requiredManager = weldContainer.select(RequiredCounterManager.class).get();
        lifeCycleCounter = weldContainer.select(EventsCounter.class).get();
        lifeCycleCounter.clear();

        transactionManager = weldContainer.select(TransactionManager.class).get();
    }

    @After
    public void after() throws SystemException {
        // cleaning the transaction state in case of an error
        if(transactionManager.getTransaction() != null
                && transactionManager.getTransaction().getStatus() == Status.STATUS_ACTIVE) {
            try {
                transactionManager.rollback();
            } catch (final Throwable ignored) {
            }
        }

        weld.shutdown();
    }

    @Test
    public void testTransactionScoped() throws Exception {
        transactionManager.begin();

        Assert.assertTrue("Expected the @Initialized scope event to be thrown",
                lifeCycleCounter.containsEvent("RequiredCounterManager.*Initialized"));
        
        Assert.assertEquals(0, counter.get());
        requiredManager.incrementCounter();
        Assert.assertEquals(1, counter.get());

        transactionManager.commit();

        Assert.assertTrue("Expected the @Destroy scope event to be thrown",
                lifeCycleCounter.containsEvent("RequiredCounterManager.*Destroyed"));

        Assert.assertFalse("Expected the Transactional observer failed and event was not proccessed correctly "
                + " as the Counter is in @Transactional scope.",
                lifeCycleCounter.containsEvent(TransactionServices.class.getName()));
    }

}
