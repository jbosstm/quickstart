/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2017, Red Hat, Inc., and individual contributors
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

package io.narayana.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.XADataSource;
import org.h2.jdbcx.JdbcDataSource;
import org.postgresql.xa.PGXADataSource;

import com.arjuna.ats.jdbc.TransactionalDriver;

/**
 * Utility class providing H2 connection
 * and operations on the test database table
 */
public class DBUtils {
    public static final java.sql.Driver TXN_DRIVER_INSTANCE = new TransactionalDriver();

    /* A hack for h2 which does not overcome byteman failing rules and leaves locked database.
     * The trouble is that byteman throws XAException and the local transaction is still active.
     * The recovery manager does not clean the connection transaction and even connection close
     * does not help. The only help is waiting for lock timeout or cleaning manually by finishing
     * local transaction on connection.
     * Tested on PostgreSQL and this hacking is not necessary there.
     */
    public static Connection h2LockConnection;

    public static final String DB_1 = "test1";
    public static final String DB_2 = "test2";

    // org.h2.jdbcx.JdbcDataSource
    private static final String DB_H2_DRIVER = "org.h2.Driver";
    private static final String DB_H2_CONNECTION = "jdbc:h2:mem:%s;DB_CLOSE_DELAY=-1";
    private static final String DB_H2_USER = "";
    private static final String DB_H2_PASSWORD = "";
    // org.postgresql.xa.PGXADataSource
    private static final String DB_PG_DRIVER = "org.postgresql.Driver";
    private static final String DB_PG_HOST = "localhost";
    private static final int DB_PG_PORT = 5432;
    private static final String DB_PG_CONNECTION = String.format("jdbc:postgresql://%s:%s/", DB_PG_HOST, DB_PG_PORT) + "%s";
    private static final String DB_PG_USER = "crashrec";
    private static final String DB_PG_PASSWORD = "crashrec";

    private static String TEST_TABLE_NAME = "TXN_DRIVER_TEST";
    private static String CREATE_TABLE = String.format("CREATE TABLE %s(id int primary key, value varchar(42))", TEST_TABLE_NAME);
    private static String DROP_TABLE = String.format("DROP TABLE %s", TEST_TABLE_NAME);
    private static String SELECT_QUERY = String.format("SELECT * FROM %s", TEST_TABLE_NAME);

    public static String INSERT_STATEMENT = String.format("INSERT INTO %s (id, value) values (?,?)", TEST_TABLE_NAME);
    public static final String DB_USER = DB_H2_USER;
    public static final String DB_PASSWORD = DB_H2_PASSWORD;

    public static Connection getConnection(String dbname) {
        return getH2Connection(dbname);
    }

    public static XADataSource getXADatasource(String dbName) {
        return getH2XADatasource(dbName);
    }

    public static int createTable(Connection conn) {
        try {
            return conn.createStatement().executeUpdate(CREATE_TABLE);
        } catch (Exception e) {
            throw new RuntimeException("Can't create table by '" + CREATE_TABLE + "'", e);
        }
    }

    public static int dropTable(Connection conn) {
        try {
            return conn.createStatement().executeUpdate(DROP_TABLE);
        } catch (Exception e) {
            throw new RuntimeException("Can't drop table by '" + DROP_TABLE + "'", e);
        }
    }

    public static ResultSet select(Connection conn) {
        try {
            return conn.createStatement().executeQuery(SELECT_QUERY);
        } catch (SQLException e) {
            System.err.println("Cannot select by '" + SELECT_QUERY + "'");
            return null;
        }
    }


    private static Connection getH2Connection(String dbname) {
        return getConnection(DB_H2_DRIVER, DB_H2_CONNECTION, dbname, DB_H2_USER, DB_H2_PASSWORD);
    }

    private static Connection getPgConnection(String dbname) {
        return getConnection(DB_PG_DRIVER, DB_PG_CONNECTION, dbname, DB_PG_USER, DB_PG_PASSWORD);
    }

    private static Connection getConnection(String driver, String formatUrl, String dbname, String user, String pass) {
        Connection dbConnection = null;
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            System.out.println(e.getMessage());
        }
        try {
            String dbConnectionUrl = String.format(formatUrl, dbname);
            dbConnection = DriverManager.getConnection(dbConnectionUrl, user, pass);
            return dbConnection;
        } catch (SQLException e) {
            throw new IllegalStateException("Can't get connection to " + formatUrl
                    + " database " + dbname + " of driver " + driver, e);
        }
    }

    private static XADataSource getH2XADatasource(String dbName) {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL(String.format(DB_H2_CONNECTION, dbName));
        ds.setUser(DB_H2_USER);
        ds.setPassword(DB_H2_PASSWORD);
        return ds;
    }
    
    private static XADataSource getPgXADatasource(String dbName) {
        PGXADataSource ds = new PGXADataSource();
        ds.setServerName(DB_PG_HOST);
        ds.setPortNumber(DB_PG_PORT);
        ds.setDatabaseName(dbName);
        ds.setUser(DB_H2_USER);
        ds.setPassword(DB_H2_PASSWORD);
        return ds;
    }
}
