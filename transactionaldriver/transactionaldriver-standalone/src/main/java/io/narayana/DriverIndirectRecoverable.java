package io.narayana;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.XADataSource;
import jakarta.transaction.TransactionManager;

import com.arjuna.ats.jdbc.TransactionalDriver;

import io.narayana.util.CodeUtils;
import io.narayana.util.DBUtils;
import io.narayana.util.TestInitialContextFactory;

/**
 * <p>
 * Usage of {@link TransactionalDriver} managing the transactionality over jdbc connection where
 * the <code>IndirectRecoverableConnection</code> implementation is used beneath.
 * <p>
 * {@link XADataSource} is registered to the Narayana transactional driver via jdni
 * name.<br>
 * This jndi name is passed as parameter of the {@link DriverManager#getConnection(String)} call,
 * the transactional driver later on search for {@link XADataSource} bound at the jndi name.
 * <p>
 * The transactional driver returns connection from that {@link XADataSource} where work
 * will be part of the local transaction which is made to be enlisted to the existing global transaction
 * which is managed by transaction manager.
 */
public class DriverIndirectRecoverable {
    private Connection conn1, conn2;
    private InitialContext ctx;

    public void process(Runnable middleAction) throws Exception {
        DriverManager.registerDriver(DBUtils.TXN_DRIVER_INSTANCE);

        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, TestInitialContextFactory.class.getName());
        ctx = new InitialContext();

        //jdbc:arjuna: <jndi name of XADataSource>
        String ds1Jndi = TransactionalDriver.arjunaDriver + "ds1";
        XADataSource dsXA1 = DBUtils.getXADatasource(DBUtils.DB_1);
        ctx.bind("ds1", dsXA1);
        Properties props1 = new Properties();
        props1.put(TransactionalDriver.userName, DBUtils.DB_USER);
        props1.put(TransactionalDriver.password, DBUtils.DB_PASSWORD);
        props1.put(TransactionalDriver.poolConnections, "false");
        props1.put(TransactionalDriver.maxConnections, "50"); // JBTM-2976
        conn1 = DriverManager.getConnection(ds1Jndi, props1);

        String ds2Jndi = TransactionalDriver.arjunaDriver + "ds2";
        XADataSource dsXA2 = DBUtils.getXADatasource(DBUtils.DB_2);
        ctx.bind("ds2", dsXA2);
        Properties props2 = new Properties();
        props2.put(TransactionalDriver.userName, DBUtils.DB_USER);
        props2.put(TransactionalDriver.password, DBUtils.DB_PASSWORD);
        props2.put(TransactionalDriver.poolConnections, "false");
        conn2 = DriverManager.getConnection(ds2Jndi, props2);

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