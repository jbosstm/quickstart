/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2016, Red Hat, Inc., and individual contributors
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

import com.arjuna.ats.internal.jta.recovery.arjunacore.XARecoveryModule;
import com.arjuna.ats.jta.recovery.XAResourceRecoveryHelper;
import org.apache.commons.dbcp2.PoolableConnection;
import org.apache.commons.dbcp2.PoolableConnectionFactory;
import org.apache.commons.dbcp2.managed.DataSourceXAConnectionFactory;
import org.apache.commons.dbcp2.managed.ManagedDataSource;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;
import javax.sql.XADataSource;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAResource;
import java.sql.SQLException;
import java.util.Hashtable;

/**
 * @author <a href="mailto:zfeng@redhat.com>Zheng Feng</a>
 */
public class TransactionalDataSourceFactory implements ObjectFactory {
    @Override
    public Object getObjectInstance(Object obj, Name name, Context context, Hashtable<?, ?> environment) throws Exception {

        if (obj == null || !(obj instanceof Reference)) {
            return null;
        }

        final Reference ref = (Reference) obj;
        if (!"javax.sql.DataSource".equals(ref.getClassName())) {
            return null;
        }

        TransactionManager transactionManager = (TransactionManager) getReferenceObject(ref, context, "transactionManager");
        XADataSource xaDataSource = (XADataSource) getReferenceObject(ref, context, "xaDataSource");

        XARecoveryModule xaRecoveryModule = getXARecoveryModule();
        if (xaRecoveryModule != null) {
            xaRecoveryModule.addXAResourceRecoveryHelper( new XAResourceRecoveryHelper() {
                @Override
                public boolean initialise(String p) throws Exception {
                    return true;
                }

                @Override
                public XAResource[] getXAResources() throws Exception {
                    try {
                        return new XAResource[] { xaDataSource.getXAConnection().getXAResource() };
                    } catch (SQLException ex) {
                        return new XAResource[0];
                    }
                }
            });
        }

        if (transactionManager != null && xaDataSource != null) {
            DataSourceXAConnectionFactory xaConnectionFactory =
                    new DataSourceXAConnectionFactory(transactionManager, xaDataSource);
            PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(xaConnectionFactory, null);
            GenericObjectPoolConfig config = new GenericObjectPoolConfig();
            GenericObjectPool<PoolableConnection> objectPool =
                    new GenericObjectPool<>(poolableConnectionFactory, config);
            poolableConnectionFactory.setPool(objectPool);
            return new ManagedDataSource<>(objectPool, xaConnectionFactory.getTransactionRegistry());
        } else {
            return null;
        }
    }

    private Object getReferenceObject(Reference ref, Context context, String prop) throws Exception {
        final RefAddr ra = ref.get(prop);
        if (ra != null) {
            return context.lookup(ra.getContent().toString());
        } else {
            return null;
        }
    }

    private XARecoveryModule getXARecoveryModule() {
        XARecoveryModule xaRecoveryModule = XARecoveryModule
                .getRegisteredXARecoveryModule();
        if (xaRecoveryModule != null) {
            return xaRecoveryModule;
        }
        throw new IllegalStateException(
                "XARecoveryModule is not registered with recovery manager");
    }
}
