package io.narayana;

import java.sql.Connection;
import java.sql.PreparedStatement;

import javax.sql.XADataSource;

import io.narayana.util.DBUtils;

/**
 * <p>
 * Class uses local transaction to work with two databases.
 * <p>
 * The local transaction is set up on jdbc connection by setting autocommit to false,
 * by the {@link Connection#setAutoCommit(boolean)} call. That way the all operation
 * on the particular connection is done in one local transaction and committed
 * on {@link Connection#commit()} call.
 * <p>
 * There is no management between two different local transaction provided.
 * If you need ACID behaviour for the two different local transaction you
 * need the transaction manager.
 * <p>
 * <i>NOTE:</i> here usage of {@link XADataSource} to get a connection is only
 * for showing way how to do so. It's expected for local transaction to run
 * directly with the {@link Connection}.
 */
public class JdbcLocalTransaction {

    public void process(Runnable middleAction) throws Exception {

        XADataSource dsXA1 = DBUtils.getXADatasource(DBUtils.DB_1);
        Connection conn1 = dsXA1.getXAConnection().getConnection();
        conn1.setAutoCommit(false);

        XADataSource dsXA2 = DBUtils.getXADatasource(DBUtils.DB_2);
        Connection conn2 = dsXA2.getXAConnection().getConnection();
        conn2.setAutoCommit(false);

        try {
            PreparedStatement ps1 = conn1.prepareStatement(DBUtils.INSERT_STATEMENT);
            ps1.setInt(1, 1);
            ps1.setString(2, "Arjuna");

            ps1.executeUpdate();

            PreparedStatement ps2 = conn2.prepareStatement(DBUtils.INSERT_STATEMENT);
            ps2.setInt(1, 1);
            ps2.setString(2, "Narayana");

            ps2.executeUpdate();

            conn1.commit();
            middleAction.run();
            conn2.commit();
        } catch (Exception e) {
            conn1.rollback();
            conn2.rollback();
        } finally {
            conn1.close();
            conn2.close();
        }
    }

}