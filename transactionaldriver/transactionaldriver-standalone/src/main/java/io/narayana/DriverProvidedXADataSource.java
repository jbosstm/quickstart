package io.narayana;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.Properties;

import javax.sql.XADataSource;
import jakarta.transaction.TransactionManager;

import com.arjuna.ats.jdbc.TransactionalDriver;

import io.narayana.util.CodeUtils;
import io.narayana.util.DBUtils;

/**
 * <p>
 * Usage of {@link TransactionalDriver} managing the transactionality over jdbc connection where
 * the <code>ProvidedXADataSourceConnection</code> implementation is used beneath.
 * <p>
 * {@link XADataSource} is registered to the Narayana transactional driver directly by
 * providing it as a property.<br>
 * The driver provides connection coming from the provided {@link XADataSource}
 * and ensures work done on the connection is part of the local transaction
 * which is enlisted to the existing global transaction managed by transaction manager.
 *
 * <p>
 * NOTE: if driver is not loaded directly when they are on classpath the manual registration could
 * be needed in similar way as it's
 * <p>
 * <code>
 * // jdbc.drivers is used by DriverManager to class load the driver classes<br>
 * // adding the h2 driver<br>
 * Properties p = System.getProperties();<br>
 * p.put("jdbc.drivers", Driver.class.getName());<br>
 * System.setProperties(p);<br>
 * // registering narayana jdbc driver to the driver manager<br>
 * DriverManager.registerDriver(new TransactionalDriver());<br>
 * </code>
 */
public class DriverProvidedXADataSource {
    private Connection conn1, conn2;

    public void process(Runnable middleAction) throws Exception {
         //jdbc:arjuna: <nothing, XADataSource is provided directly,
         // the prefix as whole URL defines only the fact we want to use the Narayana transactional driver>
        String transactionDriverDefinitionUrl = TransactionalDriver.arjunaDriver;

        XADataSource dsXA1 = DBUtils.getXADatasource(DBUtils.DB_1);
        Properties props1 = new Properties();
        props1.put(TransactionalDriver.XADataSource, dsXA1);
        props1.put(TransactionalDriver.userName, DBUtils.DB_USER);
        props1.put(TransactionalDriver.password, DBUtils.DB_PASSWORD);
        props1.put(TransactionalDriver.poolConnections, "true"); // default
        props1.put(TransactionalDriver.maxConnections, "50"); // JBTM-2976
        conn1 = DriverManager.getConnection(transactionDriverDefinitionUrl, props1);

        XADataSource dsXA2 = DBUtils.getXADatasource(DBUtils.DB_2);
        Properties props2 = new Properties();
        props2.put(TransactionalDriver.XADataSource, dsXA2);
        props2.put(TransactionalDriver.userName, DBUtils.DB_USER);
        props2.put(TransactionalDriver.password, DBUtils.DB_PASSWORD);
        conn2 = DriverManager.getConnection(transactionDriverDefinitionUrl, props2);

        TransactionManager tm = com.arjuna.ats.jta.TransactionManager.transactionManager();
        tm.begin();

        PreparedStatement ps1 = conn1.prepareStatement(DBUtils.INSERT_STATEMENT);
        ps1.setInt(1, 1);
        ps1.setString(2, "Arjuna");

        PreparedStatement ps2 = conn2.prepareStatement(DBUtils.INSERT_STATEMENT);
        ps2.setInt(1, 1);
        ps2.setString(2, "Narayana");

        try {
            ps1.executeUpdate();
            middleAction.run();
            ps2.executeUpdate();
            tm.commit();
        } catch (Exception e) {
            tm.rollback();
            throw e;
        }
    }

    public void closeConnections() {
        CodeUtils.swallowException(() -> conn1.rollback());
        CodeUtils.swallowException(() -> conn2.rollback());
        CodeUtils.closeMultiple(conn1, conn2);
    }
}