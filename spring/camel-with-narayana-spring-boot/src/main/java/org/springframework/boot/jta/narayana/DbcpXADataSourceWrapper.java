package org.springframework.boot.jta.narayana;

import org.apache.commons.dbcp2.PoolableConnection;
import org.apache.commons.dbcp2.PoolableConnectionFactory;
import org.apache.commons.dbcp2.managed.DataSourceXAConnectionFactory;
import org.apache.commons.dbcp2.managed.ManagedDataSource;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.XADataSourceWrapper;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import javax.sql.XADataSource;
import jakarta.transaction.TransactionManager;

/**
 * @author <a href="mailto:zfeng@redhat.com>Zheng Feng</a>
 */
@Configuration
public class DbcpXADataSourceWrapper implements XADataSourceWrapper {
    @Autowired
    private NarayanaRecoveryManagerBean recoveryManager;

    @Autowired
    private TransactionManager tm;

    @Override
    public DataSource wrapDataSource(XADataSource xaDataSource) throws Exception {
        DataSourceXAResourceRecoveryHelper helper = new DataSourceXAResourceRecoveryHelper(xaDataSource);
        recoveryManager.registerXAResourceRecoveryHelper(helper);
        System.out.println("register xa recovery helper " + helper);

        DataSourceXAConnectionFactory dataSourceXAConnectionFactory =
                new DataSourceXAConnectionFactory(tm, xaDataSource);
        PoolableConnectionFactory poolableConnectionFactory =
                new PoolableConnectionFactory(dataSourceXAConnectionFactory, null);
        GenericObjectPool<PoolableConnection> connectionPool =
                new GenericObjectPool<>(poolableConnectionFactory);
        poolableConnectionFactory.setPool(connectionPool);
        return new ManagedDataSource<>(connectionPool,
                dataSourceXAConnectionFactory.getTransactionRegistry());

    }
}