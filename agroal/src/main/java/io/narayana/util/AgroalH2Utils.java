package io.narayana.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.XADataSource;

import org.h2.jdbcx.JdbcDataSource;
import org.jboss.logging.Logger;

/**
 * Utility class providing H2 connection
 * and operations on the test database table for test can use it
 * to verify that commit/rollback operations were processed correctly.
 */
public class AgroalH2Utils {
    private static final Logger log = Logger.getLogger(AgroalH2Utils.class);

    public static final String DB_1 = "test1";
    public static final String DB_2 = "test2";

    public static final String DB_DRIVER = "org.h2.Driver";
    public static final String DB_XA_DATASOURCE = "org.h2.jdbcx.JdbcDataSource";
    public static final String DB_CONNECTION = "jdbc:h2:mem:%s;DB_CLOSE_DELAY=-1;LOCK_MODE=1";
    public static final String DB_USER = "";
    public static final String DB_PASSWORD = "";

    private static String TEST_TABLE_NAME = "TXN_DRIVER_TEST";
    private static String CREATE_TABLE = String.format("CREATE TABLE if not exists %s(id int primary key, name varchar(42))", TEST_TABLE_NAME);
    private static String DROP_TABLE = String.format("DROP TABLE if exists %s", TEST_TABLE_NAME);
    private static String SELECT_QUERY = String.format("SELECT * FROM %s", TEST_TABLE_NAME);
    public static String INSERT_STATEMENT = String.format("INSERT INTO %s (id, name) values (?,?)", TEST_TABLE_NAME);


    public static int createTable(Connection conn) {
        try {
            return conn.createStatement().executeUpdate(CREATE_TABLE);
        } catch (Exception e) {
            throw new RuntimeException("Can't create table by '" + CREATE_TABLE + "'", e);
        }
    }

    public static int dropTableSilently(Connection conn) {
        try {
            return conn.createStatement().executeUpdate(DROP_TABLE);
        } catch (Exception e) {
            log.debugf(e, "Can't drop table at connection '%s' with command '%s'", conn, DROP_TABLE);
            return Integer.MIN_VALUE;
        }
    }

    public static ResultSet select(Connection conn) {
        try {
            return conn.createStatement().executeQuery(SELECT_QUERY);
        } catch (SQLException sqle) {
            log.errorf(sqle, "Cannot execute select query for '%s'", SELECT_QUERY);
            return null;
        }
    }


    public static Connection getConnection(String dbname) {
        return getConnection(DB_DRIVER, DB_CONNECTION, dbname, DB_USER, DB_PASSWORD);
    }

    public static Connection getConnection(String driver, String formatUrl, String dbname, String user, String pass) {
        Connection dbConnection = null;
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException cnfe) {
            log.errorf(cnfe, "Cannot get connection for driver %s, url %s, db %s, user/pass  %s/%s",
                    driver, formatUrl, dbname, user, pass);
        }

        String dbConnectionUrl = null;
        try {
            dbConnectionUrl = String.format(formatUrl, dbname);
            dbConnection = DriverManager.getConnection(dbConnectionUrl, user, pass);
            return dbConnection;
        } catch (SQLException e) {
            throw new IllegalStateException("Can't get connection to '" + dbConnectionUrl
                    + "' database '" + dbname + "' of driver " + driver, e);
        }
    }

    public static XADataSource getXADatasource(String dbName) {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL(String.format(DB_CONNECTION, dbName));
        ds.setUser(DB_USER);
        ds.setPassword(DB_PASSWORD);
        return ds;
    }
}