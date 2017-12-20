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

import com.arjuna.ats.jdbc.TransactionalDriver;

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
        Connection conn1 = DriverManager.getConnection(transactionDriverDefinitionUrl, props1);

        XADataSource dsXA2 = DBUtils.getXADatasource(DBUtils.DB_2);
        Properties props2 = new Properties();
        props2.put(TransactionalDriver.XADataSource, dsXA2);
        props2.put(TransactionalDriver.userName, DBUtils.DB_USER);
        props2.put(TransactionalDriver.password, DBUtils.DB_PASSWORD);
        Connection conn2 = DriverManager.getConnection(transactionDriverDefinitionUrl, props2);

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
        } finally {
            conn2.close();
            // conn1.close();  // not closing the conn intentionally as H2 XA fails otherwise
            DBUtils.h2LockConnection = conn1; // hack for cleaning db locks in tests
        }
    }

}
