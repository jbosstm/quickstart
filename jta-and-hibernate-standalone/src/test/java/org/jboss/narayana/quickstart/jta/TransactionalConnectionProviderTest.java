/*
 *
 * Copyright The Narayana Authors
 *
 * SPDX-License-Identifier: LGPL-2.1-only
 *
 */
package org.jboss.narayana.quickstart.jta;

import com.arjuna.ats.jdbc.TransactionalDriver;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.hibernate.service.UnknownUnwrapTypeException;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class TransactionalConnectionProviderTest implements ConnectionProvider {

    public static final String DATASOURCE_JNDI = "java:/quickstartDataSourceTest";

    public static final String USERNAME = "sa";

    public static final String PASSWORD = "";

    private final TransactionalDriver transactionalDriver;

    public TransactionalConnectionProviderTest() {
        transactionalDriver = new TransactionalDriver();
    }

    @Override
    public Connection getConnection() throws SQLException {
        Properties properties = new Properties();
        properties.setProperty(TransactionalDriver.userName, USERNAME);
        properties.setProperty(TransactionalDriver.password, PASSWORD);
        return transactionalDriver.connect("jdbc:arjuna:" + DATASOURCE_JNDI, properties);
    }

    @Override
    public void closeConnection(Connection connection) throws SQLException {
        if (!connection.isClosed()) {
            connection.close();
        }
    }

    @Override
    public boolean supportsAggressiveRelease() {
        return false;
    }

    @Override
    public boolean isUnwrappableAs(Class aClass) {
        return getClass().isAssignableFrom(aClass);
    }

    @Override
    public <T> T unwrap(Class<T> aClass) {
        if (isUnwrappableAs(aClass)) {
            return (T) this;
        }

        throw new UnknownUnwrapTypeException(aClass);
    }

}
