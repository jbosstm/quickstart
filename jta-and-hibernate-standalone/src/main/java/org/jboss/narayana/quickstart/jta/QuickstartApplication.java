/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat, Inc., and individual contributors
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

package org.jboss.narayana.quickstart.jta;

import com.arjuna.ats.arjuna.common.ObjectStoreEnvironmentBean;
import com.arjuna.ats.arjuna.common.recoveryPropertyManager;
import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import com.arjuna.ats.internal.arjuna.recovery.AtomicActionRecoveryModule;
import com.arjuna.ats.internal.jdbc.recovery.JDBCXARecovery;
import com.arjuna.ats.internal.jta.recovery.arjunacore.XARecoveryModule;
import com.arjuna.ats.jta.common.jtaPropertyManager;
import com.arjuna.ats.jta.recovery.XAResourceRecovery;
import com.arjuna.ats.jta.utils.JNDIManager;
import com.arjuna.common.internal.util.propertyservice.BeanPopulator;
import org.h2.jdbcx.JdbcDataSource;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.jnp.server.NamingBeanImpl;

import javax.naming.InitialContext;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class QuickstartApplication {

    /**
     * JNDI server.
     */
    private static final NamingBeanImpl NAMING_BEAN = new NamingBeanImpl();

    /**
     * CDI container.
     */
    private static WeldContainer WELD_CONTAINER;

    /**
     * Quickstart demonstrations executor
     */
    private static QuickstartService QUICKSTART_SERVICE;

    public static void main(String[] args) throws Exception {
        if (args.length != 1 && args.length != 2) {
            throw new IllegalArgumentException("Invalid arguments provided. See README.md for usage examples");
        } else if (args.length == 1 && !args[0].toUpperCase().equals(CompleteAction.RECOVERY.name())) {
            throw new IllegalArgumentException("Invalid arguments provided. See README.md for usage examples");
        }

        try {
            initialise();

            switch (CompleteAction.valueOf(args[0].toUpperCase())) {
                case COMMIT:
                    QUICKSTART_SERVICE.demonstrateCommit(args[1]);
                    break;
                case ROLLBACK:
                    QUICKSTART_SERVICE.demonstrateRollback(args[1]);
                    break;
                case CRASH:
                    QUICKSTART_SERVICE.demonstrateCrash(args[1]);
                    break;
                case RECOVERY:
                    QUICKSTART_SERVICE.demonstrateRecovery();
            }
        } finally {
            shutdown();
        }
    }

    static void initialise() throws Exception {
        // Start JNDI server
        NAMING_BEAN.start();

        // Bind JTA implementation with default names
        JNDIManager.bindJTAImplementation();

        // Bind datasource
        new InitialContext().bind(TransactionalConnectionProvider.DATASOURCE_JNDI, getDataSource());

        // Set required recovery modules
        recoveryPropertyManager.getRecoveryEnvironmentBean().setRecoveryModuleClassNames(getRecoveryModuleClassNames());
        // Set recovery manager to scan every 2 seconds
        recoveryPropertyManager.getRecoveryEnvironmentBean().setPeriodicRecoveryPeriod(2);
        // Set recovery backoff between scan phases to 1 second
        recoveryPropertyManager.getRecoveryEnvironmentBean().setRecoveryBackoffPeriod(1);
        // Set XA resource recoveries required for JDBC recovery
        jtaPropertyManager.getJTAEnvironmentBean().setXaResourceRecoveries(getXaResourceRecoveries());
        // Set transaction log location
        setObjectStoreDir();
        // Delay recovery manager start until recovery demonstration
        RecoveryManager.delayRecoveryManagerThread();
        // Set recovery helper required to recover dummy XA resource
        RecoveryManager.manager().getModules().stream().filter(m -> m instanceof XARecoveryModule)
                .forEach(m -> ((XARecoveryModule) m).addXAResourceRecoveryHelper(new DummyXAResourceRecoveryHelper()));

        // Start CDI container and get quickstart service instance
        WELD_CONTAINER = new Weld().addPackage(false, QuickstartApplication.class).initialize();
        QUICKSTART_SERVICE = WELD_CONTAINER.select(QuickstartService.class).get();
    }

    static void shutdown() {
        try {
            // Stop recovery manager
            RecoveryManager.manager().terminate();
        } catch (Throwable t) {
        }

        try {
            // Close weld container
            WELD_CONTAINER.close();
        } catch (Throwable t) {
        }

        try {
            // Stop naming server
            NAMING_BEAN.stop();
        } catch (Throwable t) {
        }
    }

    static JdbcDataSource getDataSource() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:./target/quickstart.db");
        dataSource.setUser(TransactionalConnectionProvider.USERNAME);
        dataSource.setPassword(TransactionalConnectionProvider.PASSWORD);

        return dataSource;
    }

    static List<String> getRecoveryModuleClassNames() {
        List<String> recoveryModuleClassNames = new ArrayList<>();
        recoveryModuleClassNames.add(AtomicActionRecoveryModule.class.getName());
        recoveryModuleClassNames.add(XARecoveryModule.class.getName());

        return recoveryModuleClassNames;
    }

    static void setObjectStoreDir() {
        BeanPopulator.getDefaultInstance(ObjectStoreEnvironmentBean.class).setObjectStoreDir("target/tx-object-store");
        BeanPopulator.getNamedInstance(ObjectStoreEnvironmentBean.class, "communicationStore")
                .setObjectStoreDir("target/tx-object-store");
        BeanPopulator.getNamedInstance(ObjectStoreEnvironmentBean.class, "stateStore")
                .setObjectStoreDir("target/tx-object-store");
    }

    private static List<XAResourceRecovery> getXaResourceRecoveries() throws SQLException {
        XAResourceRecovery jdbcXaRecovery = new JDBCXARecovery();
        jdbcXaRecovery.initialise("jdbc-recovery.xml");

        return Collections.singletonList(jdbcXaRecovery);
    }

    private enum CompleteAction {
        COMMIT, ROLLBACK, CRASH, RECOVERY
    }

}
