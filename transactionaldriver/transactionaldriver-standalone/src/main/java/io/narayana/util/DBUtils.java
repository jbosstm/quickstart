package io.narayana.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.XADataSource;
import org.h2.jdbcx.JdbcDataSource;
import org.jboss.logging.Logger;
import org.postgresql.xa.PGXADataSource;

import com.arjuna.ats.jdbc.TransactionalDriver;

/**
 * Utility class providing H2 connection
 * and operations on the test database table
 */
public class DBUtils {
    private static final Logger log = Logger.getLogger(DBUtils.class);
    public static final java.sql.Driver TXN_DRIVER_INSTANCE = new TransactionalDriver();

    public static final String DB_1 = "test1";
    public static final String DB_2 = "test2";

    // org.h2.jdbcx.JdbcDataSource
    private static final String DB_H2_DRIVER = "org.h2.Driver";
    private static final String DB_H2_XA_DATASOURCE = "org.h2.jdbcx.JdbcDataSource";
    private static final String DB_H2_CONNECTION = "jdbc:h2:mem:%s;DB_CLOSE_DELAY=-1;LOCK_MODE=1";
    // private static final String DB_H2_CONNECTION = "jdbc:h2:tcp://localhost//tmp/%s;DB_CLOSE_DELAY=-1;LOCK_MODE=1";
    private static final String DB_H2_USER = "";
    private static final String DB_H2_PASSWORD = "";
    // org.postgresql.xa.PGXADataSource
    private static final String DB_PG_DRIVER = "org.postgresql.Driver";
    private static final String DB_PG_XA_DATASOURCE = "org.postgresql.xa.PGXADataSource";
    private static final String DB_PG_HOST = "localhost";
    private static final int DB_PG_PORT = 5432;
    private static final String DB_PG_CONNECTION = String.format("jdbc:postgresql://%s:%s/", DB_PG_HOST, DB_PG_PORT) + "%s";
    private static final String DB_PG_USER = "test";
    private static final String DB_PG_PASSWORD = "test";

    private static String TEST_TABLE_NAME = "TXN_DRIVER_TEST";
    private static String CREATE_TABLE = String.format("CREATE TABLE %s(id int primary key, value2 varchar(42))", TEST_TABLE_NAME);
    private static String DROP_TABLE = String.format("DROP TABLE %s", TEST_TABLE_NAME);
    private static String SELECT_QUERY = String.format("SELECT * FROM %s", TEST_TABLE_NAME);

    private static final boolean isH2 = true;
    public static final String DB_DRIVER = isH2 ? DB_H2_DRIVER : DB_PG_DRIVER;
    public static final String DB_XA_DATASOURCE = isH2 ? DB_H2_XA_DATASOURCE : DB_PG_XA_DATASOURCE;
    public static final String DB_CONNECTION = isH2 ? DB_H2_CONNECTION : DB_PG_CONNECTION;
    public static final String DB_USER = isH2 ? DB_H2_USER : DB_PG_USER;
    public static final String DB_PASSWORD = isH2 ? DB_H2_PASSWORD : DB_PG_PASSWORD;

    public static String INSERT_STATEMENT = String.format("INSERT INTO %s (id, value2) values (?,?)", TEST_TABLE_NAME);

    public static Connection getConnection(String dbname) {
        if(isH2) return getH2Connection(dbname);
        else return getPgConnection(dbname);
    }

    public static XADataSource getXADatasource(String dbName) {
        if (isH2) return getH2XADatasource(dbName);
        else return getPgXADatasource(dbName);
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
            e.printStackTrace();
            throw new RuntimeException("Can't drop table by '" + DROP_TABLE + "'", e);
        }
    }

    public static ResultSet select(Connection conn) {
        try {
            return conn.createStatement().executeQuery(SELECT_QUERY);
        } catch (SQLException e) {
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
        } catch (ClassNotFoundException cnfe) {
            log.errorf(cnfe, "Cannot get connection for driver: %s, url: %s, db: %s, user: %s, password: %s",
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
        ds.setUser(DB_PG_USER);
        ds.setPassword(DB_PG_PASSWORD);
        return ds;
    }
}