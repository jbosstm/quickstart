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
