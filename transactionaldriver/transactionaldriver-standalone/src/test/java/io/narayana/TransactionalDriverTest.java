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

package io.narayana;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Arrays;

import jakarta.transaction.TransactionManager;

import org.jboss.byteman.contrib.bmunit.BMScript;
import org.jboss.byteman.contrib.bmunit.BMUnitRunner;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.arjuna.ats.arjuna.common.recoveryPropertyManager;
import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import com.arjuna.ats.jta.common.jtaPropertyManager;

import io.narayana.recovery.RecoverySetupUtil;
import io.narayana.util.CodeUtils;
import io.narayana.util.DBUtils;

/**
 * Tests running commit and rollback scenarios for showcases
 * of managing database connections with use of the Narayana transaction driver.
 */
@RunWith(BMUnitRunner.class)
public class TransactionalDriverTest {
    Connection conn1, conn2;

    @Before
    public void setUp() {
        conn1 = DBUtils.getConnection(DBUtils.DB_1);
        conn2 = DBUtils.getConnection(DBUtils.DB_2);

        CodeUtils.swallowException(() -> DBUtils.dropTable(conn1));
        CodeUtils.swallowException(() -> DBUtils.dropTable(conn2));

        DBUtils.createTable(conn1);
        DBUtils.createTable(conn2);
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
        // cleaning recovery settings
        jtaPropertyManager.getJTAEnvironmentBean().setXaResourceRecoveryClassNames(null);
        // recovery modules needs to be reset for the XARecoveryModule would be instantiated again
        // and only then the value XAResourceRecovery is loaded for the next test uses new value
        recoveryPropertyManager.getRecoveryEnvironmentBean().setRecoveryModules(null);
        recoveryPropertyManager.getRecoveryEnvironmentBean().setRecoveryModuleClassNames(Arrays.asList(
            com.arjuna.ats.internal.arjuna.recovery.AtomicActionRecoveryModule.class.getName(),
            com.arjuna.ats.internal.jta.recovery.arjunacore.XARecoveryModule.class.getName()
        ));
        // closing connections
        CodeUtils.closeMultiple(conn1, conn2);
    }

    @Test
    public void localTxnCommit() throws Exception {
        new JdbcLocalTransaction().process(() -> {});

        ResultSet rs1 = DBUtils.select(conn1);
        ResultSet rs2 = DBUtils.select(conn2);
        Assert.assertTrue("First database does not contain data as expected", rs1.next());
        Assert.assertTrue("Second database does not contain data as expected", rs2.next());
    }

    @Test
    public void localTxnRollback() throws Exception {
        try {
            new JdbcLocalTransaction().process(() -> {throw new RuntimeException("expected");});
        } catch (Exception e) {
            checkcException(e);
        }

        ResultSet rs1 = DBUtils.select(conn1);
        ResultSet rs2 = DBUtils.select(conn2);
        Assert.assertTrue("First database does not contain data as expected", rs1.next());
        Assert.assertFalse("Second database contain data which is not expected", rs2.next());
    }

    @Test
    public void transactionManagerCommit() throws Exception {
        new ManagedTransaction().process(() -> {});

        ResultSet rs1 = DBUtils.select(conn1);
        ResultSet rs2 = DBUtils.select(conn2);
        Assert.assertTrue("First database does not contain data as expected to be commited", rs1.next());
        Assert.assertTrue("Second database does not contain data as expected to be commited", rs2.next());
    }

    @Test
    public void transactionManagerRollback() throws Exception {
        try {
            new ManagedTransaction().process(() -> {throw new RuntimeException("expected");});
        } catch (Exception e) {
            checkcException(e);
        }

        ResultSet rs1 = DBUtils.select(conn1);
        ResultSet rs2 = DBUtils.select(conn2);
        Assert.assertFalse("First database contains data which is not expected as rolled-back", rs1.next());
        Assert.assertFalse("Second database contains data which is not expected as rolled-back", rs2.next());
    }

    @Test
    public void transactionDriverProvidedCommit() throws Exception {
        DriverProvidedXADataSource testDs = new DriverProvidedXADataSource();
        testDs.process(() -> {});

        ResultSet rs1 = DBUtils.select(conn1);
        ResultSet rs2 = DBUtils.select(conn2);
        Assert.assertTrue("First database does not contain data as expected to be commited", rs1.next());
        Assert.assertTrue("Second database does not contain data as expected to be commited", rs2.next());

        testDs.closeConnections();
    }

    @Test
    public void transactionDriverProvidedRollback() throws Exception {
        DriverProvidedXADataSource testDs = new DriverProvidedXADataSource();
        try {
            testDs.process(() -> {throw new RuntimeException("expected");});
        } catch (Exception e) {
            checkcException(e);
        }

        ResultSet rs1 = DBUtils.select(conn1);
        ResultSet rs2 = DBUtils.select(conn2);
        Assert.assertFalse("First database contains data which is not expected as rolled-back", rs1.next());
        Assert.assertFalse("Second database contains data which is not expected as rolled-back", rs2.next());

        testDs.closeConnections();
    }

    @Test
    public void transactionDriverIndirectCommit() throws Exception {
        DriverIndirectRecoverable testDs = new DriverIndirectRecoverable();
        testDs.process(() -> {});

        ResultSet rs1 = DBUtils.select(conn1);
        ResultSet rs2 = DBUtils.select(conn2);
        Assert.assertTrue("First database does not contain data as expected to be commited", rs1.next());
        Assert.assertTrue("Second database does not contain data as expected to be commited", rs2.next());

        testDs.closeConnections();
    }

    @Test
    public void transactionDriverIndirectRollback() throws Exception {
        DriverIndirectRecoverable testDs = new DriverIndirectRecoverable();

        try {
            testDs.process(() -> {throw new RuntimeException("expected");});
        } catch (Exception e) {
            checkcException(e);
        }

        ResultSet rs1 = DBUtils.select(conn1);
        ResultSet rs2 = DBUtils.select(conn2);
        Assert.assertFalse("First database contains data which is not expected as rolled-back", rs1.next());
        Assert.assertFalse("Second database contains data which is not expected as rolled-back", rs2.next());

        testDs.closeConnections();
    }

    @Test
    public void transactionDriverDirectRecoverableCommit() throws Exception {
        DriverDirectRecoverable testDs = new DriverDirectRecoverable(); 
        testDs.process(() -> {});

        ResultSet rs1 = DBUtils.select(conn1);
        ResultSet rs2 = DBUtils.select(conn2);
        Assert.assertTrue("First database does not contain data as expected to be commited", rs1.next());
        Assert.assertTrue("Second database does not contain data as expected to be commited", rs2.next());

        testDs.closeConnections();
    }

    @Test
    public void transactionDriverDirectRecoverableRollback() throws Exception {
        DriverDirectRecoverable testDs = new DriverDirectRecoverable();
        try {
            testDs.process(() -> {throw new RuntimeException("expected");});
        } catch (Exception e) {
            checkcException(e);
        }

        ResultSet rs1 = DBUtils.select(conn1);
        ResultSet rs2 = DBUtils.select(conn2);
        Assert.assertFalse("First database contains data which is not expected as rolled-back", rs1.next());
        Assert.assertFalse("Second database contains data which is not expected as rolled-back", rs2.next());

        testDs.closeConnections();
    }
    

    /* ---------------------------------------------------------------------- */
    /* -------------------------- Recovery involed -------------------------- */

    @BMScript("xaexception.rmfail")
    @Test
    public void transactionDriverProvidedRecovery() throws Exception {
        RecoveryManager recoveryManager = RecoverySetupUtil.ds1XARecoveryIntialize();

        DriverProvidedXADataSource testDs = new DriverProvidedXADataSource();
        testDs.process(() -> {});

        ResultSet rs1 = DBUtils.select(conn1);
        Assert.assertFalse("First database " + conn1 + " is committed even commit was expected to fail", rs1.next());

        RecoverySetupUtil.runRecovery(recoveryManager);

        rs1 = DBUtils.select(conn1);
        Assert.assertTrue("First database does not contain data as expected to be commited", rs1.next());

        testDs.closeConnections();
    }

    @BMScript("xaexception.rmfail")
    @Test
    @Ignore
    public void transactionDriverDirectRecoverableRecovery() throws Exception {
        RecoveryManager recoveryManager = RecoverySetupUtil.simpleRecoveryIntialize();

        DriverDirectRecoverable testDs = new DriverDirectRecoverable();
        testDs.process(() -> {});

        ResultSet rs1 = DBUtils.select(conn1);
        Assert.assertFalse("First database " + conn1 + " is committed even commit was expected to fail", rs1.next());

        RecoverySetupUtil.runRecovery(recoveryManager);

        rs1 = DBUtils.select(conn1);
        Assert.assertTrue("First database does not contain data as expected to be commited", rs1.next());

        testDs.closeConnections();
    }

    @BMScript("xaexception.rmfail")
    @Test
    @Ignore
    public void transactionDriverIndirectRecoverableRecovery() throws Exception {
        RecoveryManager recoveryManager = RecoverySetupUtil.jdbcXARecoveryIntialize();

        DriverIndirectRecoverable testDs = new DriverIndirectRecoverable();
        testDs.process(() -> {});

        ResultSet rs1 = DBUtils.select(conn1);
        Assert.assertFalse("First database " + conn1 + " is committed even commit was expected to fail", rs1.next());

        RecoverySetupUtil.runRecovery(recoveryManager);

        rs1 = DBUtils.select(conn1);
        Assert.assertTrue("First database does not contain data as expected to be commited", rs1.next());

        testDs.closeConnections();
    }

    @BMScript("xaexception.rmfail")
    @Test
    @Ignore
    public void transactionDriverIndirectRecoverableRecovery2() throws Exception {
        RecoveryManager recoveryManager = RecoverySetupUtil.basicXARecoveryIntialize();

        DriverIndirectRecoverable testDs = new DriverIndirectRecoverable();
        testDs.process(() -> {});

        ResultSet rs1 = DBUtils.select(conn1);
        ResultSet rs2 = DBUtils.select(conn2);
        Assert.assertFalse("Both databases [" + conn1 + ", " + conn2 + "] are committed even one was expected to fail with XAException",
            rs1.next() && rs2.next());

        RecoverySetupUtil.runRecovery(recoveryManager);

        rs1 = DBUtils.select(conn1);
        Assert.assertTrue("First database does not contain data as expected to be commited", rs1.next());

        testDs.closeConnections();
    }

    private void checkcException(Exception e) {
        if (!e.getMessage().toLowerCase().contains("expected"))
            Assert.fail("Exception message does not contain 'expected' but it's '"
                + e.getClass().getName() + ":" + e.getMessage() + "'");
    }
}
