/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2018, Red Hat, Inc., and individual contributors
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

package io.narayana.recovery;

import java.sql.SQLException;
import javax.sql.XAConnection;
import javax.transaction.xa.XAResource;

import com.arjuna.ats.internal.jta.recovery.arjunacore.XARecoveryModule;
import com.arjuna.ats.jta.recovery.XAResourceRecovery;
import io.narayana.util.DBUtils;

/**
 * Simple {@link XAResourceRecovery} class which provides {@link XAResource}
 * for specific log in data. In this case it's just for particular database.
 * <br>
 * Still this helps {@link XARecoveryModule} to check in-doubt transaction at the database side
 * and try to match them to transaction in the Narayana transaction log store.
 */
public class Ds1XAResourceRecovery implements XAResourceRecovery {

    private XAConnection xaConn;
    private boolean wasReturned = false;

    @Override
    public XAResource getXAResource() throws SQLException {
        if(xaConn == null) {
            xaConn = DBUtils.getXADatasource(DBUtils.DB_1).getXAConnection();
        }
        return xaConn.getXAResource();
    }

    @Override
    public boolean initialise(String p) throws SQLException {
        return true;
    }

    @Override
    public boolean hasMoreResources() {
        wasReturned = !wasReturned;
        return wasReturned;
    }

}
