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

import java.sql.SQLException;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;

import org.jboss.logging.Logger;
import org.jboss.narayana.quickstart.jca.model.Customer;
import org.jboss.narayana.quickstart.jca.model.CustomerDAO;
import org.jboss.narayana.quickstart.jca.xa.DummyXAResource;

/**
 * Bean used to manage customers creation.
 *
 * Uses JTA during the creation of the customer. In order to show two phase commit excecution, enlists
 * <code>DummyXAResource</code> to every transaction.
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 *
 */
@ManagedBean
@RequestScoped
public class CustomerManager {

    private static final Logger LOG = Logger.getLogger(CustomerManager.class);

    private static final String DATA_SOURCE_JNDI = "java:/PostgresDS";

    private final CustomerDAO customerDAO;

    public CustomerManager() throws Throwable {
        customerDAO = new CustomerDAO(DATA_SOURCE_JNDI);
    }

    /**
     * Returns the list of all registered customers.
     *
     * @return customers list
     * @throws SQLException
     */
    public List<Customer> getCustomers() throws SQLException {
        if (LOG.isTraceEnabled()) {
            LOG.trace("CustomerManager.getCustomers()");
        }

        return customerDAO.getAll();
    }

    /**
     * Creates and registers a new customer. Customers with duplicate names are not allowed.
     *
     * Uses JTA during the creation of the customer. In order to show two phase commit excecution, enlists
     * <code>DummyXAResource</code> to every transaction.
     *
     * @param name Customer's name
     * @return "customerAdded" on success and "customerDuplicate" on failure.
     */
    public String addCustomer(final String name) throws SystemException, NotSupportedException, RollbackException {
        if (LOG.isTraceEnabled()) {
            LOG.trace("CustomerManager.addCustomer(name=" + name + ")");
        }

        final TransactionManager transactionManager = com.arjuna.ats.jta.TransactionManager.transactionManager();
        transactionManager.begin();
        transactionManager.getTransaction().enlistResource(new DummyXAResource());

        try {
            final Customer customer = new Customer(customerDAO.getNextId(), name);
            customerDAO.insert(customer);

            transactionManager.commit();

            return "customerAdded";
        } catch (Exception e) {
            LOG.warn(e.getMessage(), e);

            transactionManager.rollback();

            return "customerDuplicate";
        }
    }

}
