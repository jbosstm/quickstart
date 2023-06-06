package io.narayana;

import java.sql.Connection;
import java.sql.ResultSet;

import jakarta.transaction.TransactionManager;

import org.jboss.byteman.contrib.bmunit.BMScript;
import org.jboss.byteman.contrib.bmunit.BMUnitRunner;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.narayana.util.AgroalH2Utils;

/**
 * Tests showing that commit/rollback/recovery functionality works fine
 * when Narayana is integrated with Agroal jdbc pooling library.
 */
@RunWith(BMUnitRunner.class)
public class AgroalTest {
    Connection connForVerification1, connForVerification2;

    @Before
    public void setUp() {
        connForVerification1 = AgroalH2Utils.getConnection(AgroalH2Utils.DB_1);
        connForVerification2 = AgroalH2Utils.getConnection(AgroalH2Utils.DB_2);
        AgroalH2Utils.dropTableSilently(connForVerification1);
        AgroalH2Utils.dropTableSilently(connForVerification2);
        AgroalH2Utils.createTable(connForVerification1);
        AgroalH2Utils.createTable(connForVerification2);
    }

    @After
    public void tearDown() throws Exception {
        // cleaning possible active global transaction
        TransactionManager txn = com.arjuna.ats.jta.TransactionManager.transactionManager();
        if(txn != null) {
            if(txn.getStatus() == jakarta.transaction.Status.STATUS_ACTIVE)
                txn.rollback();
            if(txn.getStatus() != jakarta.transaction.Status.STATUS_NO_TRANSACTION)
                txn.suspend();
        }
        try {
            connForVerification1.close();
            connForVerification2.close();
        } catch (Exception ignored) {
            
            ignored.printStackTrace();
        }
    }

    @Test
    public void commit() throws Exception {
        AgroalDatasource ag = new AgroalDatasource();

        ag.process(() -> {});

    	ResultSet rs1 = AgroalH2Utils.select(connForVerification1);
    	ResultSet rs2 = AgroalH2Utils.select(connForVerification2);

    	Assert.assertTrue("First database does not contain data as expected to be commited", rs1.next());
    	Assert.assertTrue("Second database does not contain data as expected to be commited", rs2.next());
    	ag.closeConnections();
    }
    
    @Test
    public void rollback() throws Exception {
        AgroalDatasource ag = new AgroalDatasource();

    	try {
    		ag.process(() -> {throw new RuntimeException("expected");});
    	} catch (Exception e) {
    	    checkException(e);
    	}

    	ResultSet rs1 = AgroalH2Utils.select(connForVerification1);
    	ResultSet rs2 = AgroalH2Utils.select(connForVerification2);

    	Assert.assertFalse("First database contains data which is not expected as rolled-back", rs1.next());
    	Assert.assertFalse("Second database contains data which is not expected as rolled-back", rs2.next());

    	ag.closeConnections();
    }


    @BMScript("xaexception.rmfail")
    @Test
    public void recovery() throws Exception {
        AgroalDatasource ag = new AgroalDatasource();

        ag.process(() -> {});

        ResultSet rs1 = AgroalH2Utils.select(connForVerification1);
        ResultSet rs2 = AgroalH2Utils.select(connForVerification2);
        Assert.assertFalse("Both databases [" + connForVerification1 + ", " + connForVerification2 +
            "] are committed even one was expected to fail with XAException",
            rs1.next() && rs2.next());

        // manually run recovery manager to check if integration with Agroal works
        // this verifies that XAResourceRecovery was setup and if recovery finishes the failed transaction
        ag.getRecoveryManager().scan();

        rs1 = AgroalH2Utils.select(connForVerification1);
        Assert.assertTrue("First database does not contain data as expected to be commited", rs1.next());

        //after recovery cycle we need a way to close XAResource
        //see https://issues.redhat.com/browse/JBTM-3325
        ag.closeConnectionsAfterRecovery();
    }

    private void checkException(Exception e) {
        if (!e.getMessage().toLowerCase().contains("expected"))
            Assert.fail("Exception message does not contain 'expected' but it's '"
                + e.getClass().getName() + ":" + e.getMessage() + "'");
    }
}