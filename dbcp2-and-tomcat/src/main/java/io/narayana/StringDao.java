/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2016, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package io.narayana;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class StringDao {

    private static final String CREATE_TABLE_QUERY = "CREATE TABLE IF NOT EXISTS strings (string VARCHAR(500))";

    private static final String FIND_ALL_STRINGS_QUERY = "SELECT string FROM strings";

    private static final String DELETE_ALL_STRINGS_QUERY = "DELETE FROM strings";

    private static final String INSERT_STRING_QUERY = "INSERT INTO strings VALUES ('%s')";

    private Connection connection;

    public StringDao() throws SQLException {
        initDatabase();
    }

    /**
     * Get all strings from the database.
     * 
     * @return
     * @throws SQLException
     */
    public List<String> getAll() throws SQLException {
        List<String> strings = new LinkedList<>();
        getConnection();
        try (Statement statement = connection.createStatement()) {
            try (ResultSet resultSet = statement.executeQuery(FIND_ALL_STRINGS_QUERY)) {
                while (resultSet.next()) {
                    strings.add(resultSet.getString("string"));
                }
            }
        }
        close();

        return strings;
    }

    /**
     * Save string to the database.
     * @param string
     * @throws SQLException
     */
    public void save(String string) throws SQLException {
        getConnection();
        try (Statement statement = connection.createStatement()) {
            statement.execute(String.format(INSERT_STRING_QUERY, string));
        }
        close();
    }

    /**
     * Delete all strings in the database.
     * @throws SQLException
     */
    public void removeAll() throws SQLException {
        getConnection();
        try (Statement statement = connection.createStatement()) {
            statement.execute(DELETE_ALL_STRINGS_QUERY);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        close();

    }

    /**
     * Create strings table if it doesn't exist.
     */
    private void initDatabase() throws SQLException {
        getConnection();
        try (Statement statement = connection.createStatement()) {
            statement.execute(CREATE_TABLE_QUERY);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        close();
    }

    private void getConnection() {
        if (connection == null) {
            try {
                DataSource ds = InitialContext.doLookup("java:comp/env/transactionalDataSource");
                connection = ds.getConnection();
            } catch (NamingException e) {
                throw new RuntimeException(e);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void close() throws SQLException {
        if (connection != null) {
            connection.close();
            connection = null;
        }
    }
}
