package io.narayana;

import java.sql.PreparedStatement;

import javax.sql.XAConnection;
import javax.sql.XADataSource;
import jakarta.transaction.TransactionManager;
import javax.transaction.xa.XAResource;

import io.narayana.util.DBUtils;

/**
 * <p>
 * Using transaction manager to manage two local database transaction.
 * In difference to {@link JdbcLocalTransaction} we need to enlist the
 * {@link XAResource} with the transaction manager. Transaction manager
 * then ensures the operations on the connection to be run in local
 * transaction. At the end these local transactions are managed
 * for the ACID would be guaranteed.
 */
public class ManagedTransaction {

    public void process(Runnable middleAction) throws Exception {
        TransactionManager tm = com.arjuna.ats.jta.TransactionManager.transactionManager();
        tm.begin();

        XADataSource dsXA1 = DBUtils.getXADatasource(DBUtils.DB_1);
        XAConnection xaConn1 = dsXA1.getXAConnection();
        tm.getTransaction().enlistResource(xaConn1.getXAResource());
        PreparedStatement ps1 = xaConn1.getConnection()
                .prepareStatement(DBUtils.INSERT_STATEMENT);
        ps1.setInt(1, 1);
        ps1.setString(2, "Arjuna");

        XADataSource dsXA2 = DBUtils.getXADatasource(DBUtils.DB_2);
        XAConnection xaConn2 = dsXA2.getXAConnection();
        tm.getTransaction().enlistResource(xaConn2.getXAResource());
        PreparedStatement ps2 = xaConn2.getConnection()
                .prepareStatement(DBUtils.INSERT_STATEMENT);
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
        } finally {
            xaConn1.close();
            xaConn2.close();
        }
    }

}