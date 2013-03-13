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
package org.jboss.narayana.quickstart.jca.model;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.jboss.logging.Logger;
import org.jboss.narayana.quickstart.jca.exception.DuplicateException;

/**
 * Manages all database operations.
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 *
 */
public final class CustomerDAO {

    private static final Logger LOG = Logger.getLogger(CustomerDAO.class);

    private final String dataSourceJndi;

    public CustomerDAO(final String dataSourceJndi) throws SQLException {
        this.dataSourceJndi = dataSourceJndi;
        init();
    }

    /**
     * Returns next available customer id.
     *
     * @return Customer id
     * @throws SQLException
     */
    public int getNextId() throws SQLException {
        if (LOG.isTraceEnabled()) {
            LOG.trace("CustomerDAO.getNextId()");
        }

        int id = 1;
        final Connection connection = getConnection();
        final PreparedStatement statement = connection.prepareStatement("SELECT id FROM customer ORDER BY id DESC LIMIT 1;");
        final ResultSet resultSet = statement.executeQuery();

        if (resultSet.next()) {
            id = resultSet.getInt("id") + 1;
        }

        connection.close();

        return id;
    }

    /**
     * Returns customer by id provided.
     *
     * @param id Customer id
     * @return Customer instance if such customer exists and null otherwise.
     * @throws SQLException
     */
    public Customer get(final int id) throws SQLException {
        if (LOG.isTraceEnabled()) {
            LOG.trace("CustomerDAO.get(id=" + id + ")");
        }

        final Connection connection = getConnection();
        final PreparedStatement statement = connection.prepareStatement("SELECT * FROM customer WHERE id = '" + id
                + "' LIMIT 1;");
        final ResultSet resultSet = statement.executeQuery();
        final Customer customer = resultSetToCustomer(resultSet);

        connection.close();

        return customer;
    }

    /**
     * Returns all registered customers.
     *
     * @return List of customers.
     * @throws SQLException
     */
    public List<Customer> getAll() throws SQLException {
        if (LOG.isTraceEnabled()) {
            LOG.trace("CustomerDAO.getAll()");
        }

        final Connection connection = getConnection();
        final PreparedStatement statement = connection.prepareStatement("SELECT * FROM customer;");
        final ResultSet resultSet = statement.executeQuery();
        final List<Customer> customers = resultSetToCustomers(resultSet);

        connection.close();

        return customers;
    }

    /**
     * Inserts new customer to the database.
     *
     * @param customer New customer
     * @throws SQLException
     * @throws DuplicateException Throws exception if customer with the same name and/or id is already registered.
     */
    public void insert(final Customer customer) throws SQLException, DuplicateException {
        if (LOG.isTraceEnabled()) {
            LOG.trace("CustomerDAO.insert(customer=" + customer + ")");
        }

        if (isDuplicate(customer)) {
            throw new DuplicateException("Customers with duplicate names and/or IDs are not allowed. Customer: " + customer);
        }

        final Connection connection = getConnection();
        final PreparedStatement statement = connection.prepareStatement("INSERT INTO customer VALUES ('" + customer.getId()
                + "', '" + customer.getName() + "');");

        statement.executeUpdate();
        connection.close();
    }

    /**
     * Updates already registered customer.
     *
     * @param customer
     * @throws SQLException
     * @throws DuplicateException Throws exception if customer with the same name is already registered.
     */
    public void update(final Customer customer) throws SQLException, DuplicateException {
        if (LOG.isTraceEnabled()) {
            LOG.trace("CustomerDAO.update(customer=" + customer + ")");
        }

        final Customer currentCustomer = get(customer.getId());

        if (isDuplicateName(customer) && !currentCustomer.equals(customer)) {
            throw new DuplicateException("Customers with duplicate names are not allowed. Customer: " + customer);
        }

        final Connection connection = getConnection();
        final PreparedStatement statement = connection.prepareStatement("UPDATE customer SET name = '" + customer.getName()
                + "' WHERE id = '" + customer.getId() + "';");

        statement.executeUpdate();
        connection.close();
    }

    /**
     * Deletes customer.
     *
     * @param customer
     * @throws SQLException
     */
    public void delete(final Customer customer) throws SQLException {
        if (LOG.isTraceEnabled()) {
            LOG.trace("CustomerDAO.delete(customer=" + customer + ")");
        }

        final Connection connection = getConnection();
        final PreparedStatement statement = connection.prepareStatement("DELETE FROM customer WHERE id = '" + customer.getId()
                + "';");

        statement.executeUpdate();
        connection.close();
    }

    /**
     * Deletes all customers.
     *
     * @throws SQLException
     */
    public void clear() throws SQLException {
        if (LOG.isTraceEnabled()) {
            LOG.trace("CustomerDAO.clear()");
        }

        final Connection connection = getConnection();
        final PreparedStatement statement = connection.prepareStatement("DELETE FROM customer;");

        statement.executeUpdate();
        connection.close();
    }

    private void init() throws SQLException {
        final Connection connection = getConnection();

        if (!tableExists(connection)) {
            createTable(connection);
        }

        connection.close();
    }

    private void createTable(final Connection connection) throws SQLException {
        final PreparedStatement statement = connection.prepareStatement("CREATE TABLE customer (id INTEGER, name VARCHAR);");

        statement.executeUpdate();
    }

    private boolean tableExists(final Connection connection) throws SQLException {
        final DatabaseMetaData dbm = connection.getMetaData();
        ResultSet tables = dbm.getTables(null, null, "customer", null);

        boolean tableExists = tables.next();

        if (!tableExists) {
            // H2 database uses capitals for table names
            tables = dbm.getTables(null, null, "CUSTOMER", null);
            tableExists = tables.next();
        }

        return tableExists;
    }

    private Customer resultSetToCustomer(final ResultSet resultSet) {
        Customer customer = null;

        try {
            if (resultSet.next()) {
                final int id = resultSet.getInt("id");
                final String name = resultSet.getString("name");
                customer = new Customer(id, name);
            }
        } catch (SQLException e) {
            LOG.warn(e.getMessage(), e);
        }

        return customer;
    }

    private List<Customer> resultSetToCustomers(final ResultSet resultSet) {
        final List<Customer> customers = new ArrayList<Customer>();

        Customer customer = resultSetToCustomer(resultSet);
        while (customer != null) {
            customers.add(customer);
            customer = resultSetToCustomer(resultSet);
        }

        return customers;
    }

    private Connection getConnection() {
        Connection connection = null;
        Context context = null;

        try {
            context = new InitialContext();
            final DataSource dataSource = (DataSource) context.lookup(dataSourceJndi);
            connection = dataSource.getConnection();
        } catch (Exception e) {
            LOG.fatal(e.getMessage(), e);
            throw new RuntimeException("Cannot get JDBC connection", e);
        } finally {
            try {
                if (context != null) {
                    context.close();
                }
            } catch (NamingException e) {
                LOG.warn(e.getMessage(), e);
            }
        }

        return connection;
    }

    private boolean isDuplicate(final Customer customer) throws SQLException {
        final Connection connection = getConnection();
        final PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM customer WHERE LOWER(name) = '"
                + customer.getName().toLowerCase() + "' OR id = '" + customer.getId() + "';");
        final ResultSet resultSet = statement.executeQuery();
        boolean isDuplicate = false;

        if (resultSet.next()) {
            isDuplicate = resultSet.getInt(1) > 0;
        }

        connection.close();

        return isDuplicate;
    }

    private boolean isDuplicateName(final Customer customer) throws SQLException {
        final Connection connection = getConnection();
        final PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM customer WHERE LOWER(name) = '"
                + customer.getName().toLowerCase() + "';");
        final ResultSet resultSet = statement.executeQuery();
        boolean isDuplicate = false;

        if (resultSet.next()) {
            isDuplicate = resultSet.getInt(1) > 0;
        }

        connection.close();

        return isDuplicate;
    }

}
