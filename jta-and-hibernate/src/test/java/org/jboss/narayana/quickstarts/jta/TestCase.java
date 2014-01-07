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

import com.arjuna.ats.jta.utils.JNDIManager;
import com.arjuna.ats.jta.common.jtaPropertyManager;
import junit.framework.Assert;
import org.h2.jdbcx.JdbcDataSource;
import org.jboss.narayana.quickstarts.jta.jpa.TestEntity;
import org.jboss.narayana.quickstarts.jta.jpa.TestEntityRepository;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.jnp.interfaces.NamingParser;
import org.jnp.server.NamingBeanImpl;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.naming.InitialContext;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.TransactionalException;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class TestCase {

    private static final NamingBeanImpl NAMING_BEAN = new NamingBeanImpl();

    private TransactionManager transactionManager;

    private RequiredCounterManager requiredCounterManager;

    private MandatoryCounterManager mandatoryCounterManager;

    private TestEntityRepository testEntityRepository;

    private Weld weld;

    @BeforeClass
    public static void beforeClass() throws Exception {
        // Start JNDI server
        NAMING_BEAN.start();

        // Bind JTA implementation with default names
        JNDIManager.bindJTAImplementation();

        // Bind JTA implementation with JBoss names. Needed for JTA 1.2 implementation.
        // See https://issues.jboss.org/browse/JBTM-2054
        NAMING_BEAN.getNamingInstance().createSubcontext(new NamingParser().parse("jboss"));
        jtaPropertyManager.getJTAEnvironmentBean().setTransactionManagerJNDIContext("java:/jboss/TransactionManager");
        jtaPropertyManager.getJTAEnvironmentBean()
                .setTransactionSynchronizationRegistryJNDIContext("java:/jboss/TransactionSynchronizationRegistry");
        JNDIManager.bindJTAImplementation();

        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:test;MODE=Oracle;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
        dataSource.setUser("sa");
        dataSource.setPassword("");
        InitialContext context = new InitialContext();
        context.bind("java:/jboss/testDS1", dataSource);
    }

    @AfterClass
    public static void afterClass() {
        // Stop JNDI server
        NAMING_BEAN.stop();
    }

    @Before
    public void before() throws Exception {
        transactionManager = (TransactionManager) new InitialContext().lookup("java:/TransactionManager");

        // Initialize Weld container
        weld = new Weld();
        final WeldContainer weldContainer = weld.initialize();

        // Bootstrap our beans
        requiredCounterManager = weldContainer.instance().select(RequiredCounterManager.class).get();
        mandatoryCounterManager = weldContainer.instance().select(MandatoryCounterManager.class).get();
        testEntityRepository = weldContainer.instance().select(TestEntityRepository.class).get();
    }

    @After
    public void after() {
        try {
            transactionManager.rollback();
        } catch (final Throwable t) {
        }

        // Shutdown Weld container
        weld.shutdown();
    }

    @Test
    public void testRequiredTransactionWithExistingTransaction() throws Exception {
        transactionManager.begin();
        Assert.assertEquals(true, requiredCounterManager.isTransactionAvailable());
        transactionManager.rollback();
    }

    @Test
    public void testRequiredTransactionWithoutExistingTransaction() {
        Assert.assertEquals(true, requiredCounterManager.isTransactionAvailable());
    }

    @Test
    public void testMandatoryTransactionWithExistingTransaction() throws Exception {
        transactionManager.begin();
        Assert.assertEquals(true, mandatoryCounterManager.isTransactionAvailable());
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
    }

    @Test
    public void testJpa() {
        System.out.println(testEntityRepository.save(new TestEntity("test1")));
        System.out.println(testEntityRepository.save(new TestEntity("test2")));
        System.out.println(testEntityRepository.save(new TestEntity("test3")));
        org.junit.Assert.assertEquals(3, testEntityRepository.findAll().size());
    }

}
