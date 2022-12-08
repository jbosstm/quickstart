/*
 * Copyright The Narayana Authors
 *
 * SPDX-License-Identifier: LGPL-2.1-only
 */
package org.jboss.narayana.jta.quickstarts;

import com.arjuna.ats.arjuna.common.MetaObjectStoreEnvironmentBean;
import com.arjuna.ats.internal.arjuna.objectstore.jdbc.JDBCStore;
import com.arjuna.ats.internal.arjuna.objectstore.jdbc.accessors.DynamicDataSourceJDBCAccess;
import com.arjuna.common.internal.util.propertyservice.BeanPopulator;
import jakarta.transaction.RollbackException;
import jakarta.transaction.SystemException;
import jakarta.transaction.TransactionManager;
import jakarta.transaction.UserTransaction;

public class JDBCStoreExample {

    public static void main(String[] args) throws Exception {
        // the Narayana transaction store is configurable, set it up to use a database
        setupStore(Boolean.getBoolean("USE_JBOSSTS_PROPERTIES"));

        UserTransaction utx = com.arjuna.ats.jta.UserTransaction.userTransaction();

        // start a transaction, enlist some resources with it and then commit it
        utx.begin();
        enlistResources();
        utx.commit();
    }

    public static void enlistResources() throws SystemException, RollbackException {
        // resource enlistment is performed via the TransactionManager API
        TransactionManager tm = com.arjuna.ats.jta.TransactionManager.transactionManager();
        // create two resources, although they won't actually do anything, they will force a log record to be created
        DummyXAResource xar1 = new DummyXAResource();
        DummyXAResource xar2 = new DummyXAResource();

        // and enlist them with the transaction
        tm.getTransaction().enlistResource(xar1);
        tm.getTransaction().enlistResource(xar2);
    }

    private static void setupStore(boolean usePropertiesFile) {
        if (usePropertiesFile) {
            setupStoreViaPropertiesFile();
        } else {
            setupStoreViaConfigBean();
        }
    }

    public static void setupStoreViaConfigBean() {
        final String jdbcStoreClass = JDBCStore.class.getName();
        final String jdbcAccess = DynamicDataSourceJDBCAccess.class.getName();
        final String DB_CLASSNAME = "org.h2.jdbcx.JdbcDataSource";
        final String DB_URL = "jdbc:h2:file:./target/h2/JBTMDB";
        final String dataSourceJndiName = String.format("%s;ClassName=%s;URL=%s;User=sa;Password=sa",
                jdbcAccess, DB_CLASSNAME, DB_URL);

        // Narayana uses environment beans to configure the store used to persist transaction logs.
        // Although it uses various stores for persisting different categories of information
        // (the default store, stateStore and communicationStore) there is a bean which propagates
        // config settings to all relevant store config beans:
        final MetaObjectStoreEnvironmentBean metaObjectStoreEnvironmentBean =
                BeanPopulator.getDefaultInstance(MetaObjectStoreEnvironmentBean.class);

        // set the store type to the JDBC store
        metaObjectStoreEnvironmentBean.setObjectStoreType(jdbcStoreClass);
        // set the JNDI name of the datasource to be  used to access the database
        metaObjectStoreEnvironmentBean.setJdbcAccess(dataSourceJndiName);
    }

    public static void setupStoreViaPropertiesFile() {
        // the same config can be specified using jbossts properties files:
        if (System.getProperty("com.arjuna.ats.arjuna.common.propertiesFile") == null) {
            System.setProperty("com.arjuna.ats.arjuna.common.propertiesFile", "h2-jbossts-properties.xml");
        }
    }
}
