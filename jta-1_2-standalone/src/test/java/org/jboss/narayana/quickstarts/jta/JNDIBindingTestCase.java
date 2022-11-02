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

import javax.naming.Context;
import jakarta.transaction.Status;
import jakarta.transaction.SystemException;
import jakarta.transaction.Transaction;
import jakarta.transaction.TransactionManager;
import jakarta.transaction.TransactionSynchronizationRegistry;
import jakarta.transaction.TransactionalException;
import jakarta.transaction.UserTransaction;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.jnp.server.NamingBeanImpl;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.arjuna.ats.jta.utils.JNDIManager;

/**
 * <p>
 * This test case shows how to initiate the Narayana transaction manager
 * when used with the CDI standalone container.
 * </p>
 * <p>
 * Narayana configuration can be provided by the <code>jbossts-properties.xml</code>
 * and/or (re)defined programatically setting up properties to particular
 * configuration bean. You can see this in {@link #beforeClass()}.
 * </p>
 * <p>
 * The standalone CDI container is <a href="http://weld.cdi-spec.org">Weld</a>
 * is started in {@link #before()}. The JTA CDI extension needs
 * to find out the implementation of the {@link TransactionManager},
 * {@link TransactionSynchronizationRegistry} and {@link UserTransaction}
 * which is in this case bound to JNDI {@link JNDIManager#bindJTAImplementation()}.
 * The JTA CDI extension finds out the configured instances with JNDI lookup.
 * </p>
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class JNDIBindingTestCase {

    private static final NamingBeanImpl NAMING_BEAN = new NamingBeanImpl();

    private Weld weld;
    private TransactionManager transactionManager;

    private RequiredCounterManager requiredCounterManager;
    private MandatoryCounterManager mandatoryCounterManager;
    private EventsCounter lifeCycleCounter;


    @BeforeClass
    public static void beforeClass() throws Exception {
        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.jnp.interfaces.NamingContextFactory");
        System.setProperty(Context.URL_PKG_PREFIXES, "org.jboss.naming:org.jnp.interfaces");

        // Start JNDI server
        NAMING_BEAN.start();

        // Bind JTA implementation with default names
        JNDIManager.bindJTAImplementation();
    }

    @AfterClass
    public static void afterClass() {
        NAMING_BEAN.stop();

        System.clearProperty(Context.INITIAL_CONTEXT_FACTORY);
        System.clearProperty(Context.URL_PKG_PREFIXES);
    }

    @Before
    public void before() throws Exception {
        // Initialize Weld container
        weld = new Weld();
        final WeldContainer weldContainer = weld.initialize();

        // Bootstrap the beans
        requiredCounterManager = weldContainer.select(RequiredCounterManager.class).get();
        mandatoryCounterManager = weldContainer.select(MandatoryCounterManager.class).get();

        lifeCycleCounter = weldContainer.select(EventsCounter.class).get();
        lifeCycleCounter.clear();

        transactionManager = weldContainer.select(TransactionManager.class).get();
    }

    @After
    public void after() throws SystemException {
        // cleaning the transaction state in case of an error
        if(transactionManager.getTransaction()!=null
                && transactionManager.getTransaction().getStatus() == Status.STATUS_ACTIVE) {
            try {
                transactionManager.rollback();
            } catch (final Throwable ignored) {
            }
        }

        weld.shutdown();
    }

    @Test
    public void testRequiredTransactionWithExistingTransaction() throws Exception {
        transactionManager.begin();
        Assert.assertTrue(requiredCounterManager.isTransactionAvailable());
        transactionManager.rollback();
    }

    @Test
    public void testRequiredTransactionWithoutExistingTransaction() {
        Assert.assertTrue(requiredCounterManager.isTransactionAvailable());

        Assert.assertTrue(lifeCycleCounter.containsEvent("RequiredCounterManager.*Initialized"));
        Assert.assertTrue(lifeCycleCounter.containsEvent("RequiredCounterManager.*Destroyed"));
    }

    @Test
    public void testMandatoryTransactionWithExistingTransaction() throws Exception {
        transactionManager.begin();
        Assert.assertTrue(mandatoryCounterManager.isTransactionAvailable());
        transactionManager.rollback();
    }

    @Test(expected = TransactionalException.class)
    public void testMandatoryTransactionWithoutExistingTransaction() {
        mandatoryCounterManager.isTransactionAvailable();
    }

    @Test
    public void testTransactionScoped() throws Exception {
        transactionManager.begin();
        Assert.assertEquals(0, requiredCounterManager.getCounter());
        Assert.assertEquals(0, mandatoryCounterManager.getCounter());
        requiredCounterManager.incrementCounter();
        Assert.assertEquals(1, requiredCounterManager.getCounter());
        Assert.assertEquals(1, mandatoryCounterManager.getCounter());

        Assert.assertTrue("Expected the @Initialized scope event to be thrown",
                lifeCycleCounter.containsEvent("RequiredCounterManager.*Initialized"));

        final Transaction suspendedTransaction = transactionManager.suspend();

        transactionManager.begin();
        Assert.assertEquals(0, requiredCounterManager.getCounter());
        Assert.assertEquals(0, mandatoryCounterManager.getCounter());
        mandatoryCounterManager.incrementCounter();
        Assert.assertEquals(1, requiredCounterManager.getCounter());
        Assert.assertEquals(1, mandatoryCounterManager.getCounter());

        transactionManager.rollback();
        transactionManager.resume(suspendedTransaction);
        transactionManager.rollback();

        Assert.assertTrue("Expected the @Destroy scope event to be thrown",
                lifeCycleCounter.containsEvent("RequiredCounterManager.*Destroyed"));
    }

}
