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

import java.sql.SQLException;

import javax.transaction.TransactionManager;

import org.jboss.narayana.quickstart.jca.common.AbstractTest;
import org.jboss.narayana.quickstart.jca.model.Customer;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 *
 */
public class XAResourcesTest extends AbstractTest {

    private DummyXAResource dummyXAResource;

    private TransactionManager transactionManager;

    @Before
    public void before() throws Exception {
        transactionManager = com.arjuna.ats.jta.TransactionManager.transactionManager();
        transactionManager.begin();
        super.before();
        dummyXAResource = new DummyXAResource();
        transactionManager.getTransaction().enlistResource(dummyXAResource);
    }

    @After
    public void after() throws SQLException {
        try {
            transactionManager.rollback();
        } catch (Exception e) {
        }
        super.after();
    }

    @Test
    public void testCommitWithBothResources() throws Exception {
        final Customer customer = new Customer(1, "First customer name");
        customerDAO.insert(customer);

        final int counterBefore = dummyXAResource.getCommitedTransactionsCounter();

        transactionManager.commit();

        final int counterAfter = dummyXAResource.getCommitedTransactionsCounter();

        Assert.assertEquals(counterBefore + 1, counterAfter);
        Assert.assertEquals(customer, customerDAO.get(1));
    }

    @Test
    public void testRollbackWithBothResources() throws Exception {
        final Customer customer = new Customer(1, "First customer name");
        customerDAO.insert(customer);

        final int counterBefore = dummyXAResource.getCommitedTransactionsCounter();

        transactionManager.rollback();

        final int counterAfter = dummyXAResource.getCommitedTransactionsCounter();

        Assert.assertEquals(counterBefore, counterAfter);
        Assert.assertEquals(null, customerDAO.get(1));
    }

}
