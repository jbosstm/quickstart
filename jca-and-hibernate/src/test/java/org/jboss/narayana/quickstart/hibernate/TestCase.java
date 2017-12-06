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

import org.jboss.jca.embedded.Embedded;
import org.jboss.jca.embedded.EmbeddedFactory;
import org.junit.*;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class TestCase {

    /**
     * JCA resource adapter for XA datasources.
     */
    private static final String JDBC_RAR_FILE_PATH = "src/test/resources/jdbc-xa.rar";

    /**
     * H2 database datasource.
     */
    private static final String DATA_SOURCE_FILE_PATH = "src/test/resources/h2-xa-ds.xml";

    /**
     * Embedded IronJacamar container instance.
     */
    private static Embedded EMBEDDED;

    /**
     * Hibernate entity manager factory used by <code>CustomerManager</code> to create <code>EntityManager</code> instances.
     */
    private EntityManagerFactory entityManagerFactory;

    /**
     * Customers manager, used to get/create/delete customers from the database.
     */
    private CustomerManager customerManager;

    /**
     * Manager used to interact with <code>DummyXAResource</code>.
     */
    private DummyXAResourceManager dummyXAResourceManager;


    /**
     * Starts the container and deploys resource adapter with datasource.
     *
     * @throws Throwable
     */
    @BeforeClass
    public static void beforeClass() throws Throwable {
        EMBEDDED = EmbeddedFactory.create();
        EMBEDDED.startup();
        EMBEDDED.deploy(getURL(JDBC_RAR_FILE_PATH));
        EMBEDDED.deploy(getURL(DATA_SOURCE_FILE_PATH));
    }

    /**
     * Undeploys datasource with resource adapter and shuts down the container.
     *
     * @throws Throwable
     */
    @AfterClass
    public static void afterClass() throws Throwable {
        EMBEDDED.undeploy(getURL(DATA_SOURCE_FILE_PATH));
        EMBEDDED.undeploy(getURL(JDBC_RAR_FILE_PATH));
        EMBEDDED.shutdown();
    }

    @Before
    public void before() throws Exception {
        entityManagerFactory = Persistence.createEntityManagerFactory("org.jboss.narayana.quickstart.hibernate");
        customerManager = new CustomerManager(entityManagerFactory);
        dummyXAResourceManager = new DummyXAResourceManager();
        customerManager.clear();
    }

    @After
    public void after() throws SQLException {
        entityManagerFactory.close();
    }

    /**
     * Adds one customer to the database.
     *
     * As a result, customer with name John will be added to the database and the dummy XA resource's successfully
     * commited transactions counter will be incremented by 1.
     *
     * @throws Exception
     */
    @Test
    public void testAddCustomer() throws Exception {
        final int transactionsCountBefore = dummyXAResourceManager.getCommitedTransactionsCounter();
        final boolean result = customerManager.addCustomer("John");
        Assert.assertEquals(true, result);
        Assert.assertEquals(transactionsCountBefore + 1, dummyXAResourceManager.getCommitedTransactionsCounter());

        final List<Customer> customers = customerManager.getCustomers();
        Assert.assertEquals(1, customers.size());
        Assert.assertEquals("John", customers.get(0).getName());
    }

    /**
     * Adds customer named Peter to the database and then tries to add another customer with the same name.
     *
     * As a result, only one customer will be added successfully and successfully commited transactions counter will
     * be incremented only by 1.
     *
     * @throws Exception
     */
    @Test
    public void testAddDuplicateCustomer() throws Exception {
        int transactionsCountBefore = dummyXAResourceManager.getCommitedTransactionsCounter();
        boolean result = customerManager.addCustomer("Peter");
        Assert.assertEquals(true, result);
        Assert.assertEquals(transactionsCountBefore + 1, dummyXAResourceManager.getCommitedTransactionsCounter());

        transactionsCountBefore = dummyXAResourceManager.getCommitedTransactionsCounter();
        result = customerManager.addCustomer("Peter");
        Assert.assertEquals(false, result);
        Assert.assertEquals(transactionsCountBefore, dummyXAResourceManager.getCommitedTransactionsCounter());

        final List<Customer> customers = customerManager.getCustomers();
        Assert.assertEquals(1, customers.size());
        Assert.assertEquals("Peter", customers.get(0).getName());
    }

    private static URL getURL(final String path) throws MalformedURLException {
        final File f = new File(path);

        return f.toURI().toURL();
    }
}
