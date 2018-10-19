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
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.Properties;

import javax.sql.XADataSource;
import javax.transaction.TransactionManager;

import com.arjuna.ats.internal.jdbc.drivers.PropertyFileDynamicClass;
import com.arjuna.ats.jdbc.TransactionalDriver;

import io.narayana.util.CodeUtils;
import io.narayana.util.DBUtils;

/**
 * <p>
 * Usage of {@link TransactionalDriver} managing the transactionality over jdbc connection where
 * the <code>DirectRecoverableConnection</code> implementation is used beneath.
 * <p>
 * Properties file is provided to the Narayana transactional driver which dynamically
 * creates a {@link XADataSource}.<br>
 * The path to the properties file is passed as parameter of the {@link DriverManager#getConnection(String)} call,
 * the transactional driver later creates brandly new {@link XADataSource} based of the information in properties file.<br>
 * It assumes there is property <code>xaDataSourceClassName</code> which defines implementation of {@link XADataSource}
 * that will be intialized and names of <code>setters</code> that will be called with use of reflection
 * and filled with values defined in the properties file.
 * <p>
 * The transactional driver returns connection from that {@link XADataSource},
 * work on the returned connection will be part of the local transaction which
 * is made to be enlisted to the existing global transaction which is managed by transaction manager.
 */
public class DriverDirectRecoverable {
    private Connection conn1, conn2;

    public void process(Runnable middleAction) throws Exception {
        DriverManager.registerDriver(DBUtils.TXN_DRIVER_INSTANCE);

        //jdbc:arjuna: <path to properties file>
        String jdbcUrl1 = TransactionalDriver.arjunaDriver + "target/classes/ds1.h2.properties";
        Properties props1 = new Properties();
        props1.put(TransactionalDriver.dynamicClass, PropertyFileDynamicClass.class.getName());
        props1.put(TransactionalDriver.userName, DBUtils.DB_USER);
        props1.put(TransactionalDriver.password, DBUtils.DB_PASSWORD);
        props1.put(TransactionalDriver.poolConnections, "true"); // default
        props1.put(TransactionalDriver.maxConnections, "50"); // JBTM-2976
        conn1 = DriverManager.getConnection(jdbcUrl1, props1);

        String jdbcUrl2 = TransactionalDriver.arjunaDriver + "target/classes/ds2.h2.properties";
        Properties props2 = new Properties();
        props2.put(TransactionalDriver.dynamicClass, PropertyFileDynamicClass.class.getName());
        props2.put(TransactionalDriver.userName, DBUtils.DB_USER);
        props2.put(TransactionalDriver.password, DBUtils.DB_PASSWORD);
        conn2 = DriverManager.getConnection(jdbcUrl2, props2);

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
