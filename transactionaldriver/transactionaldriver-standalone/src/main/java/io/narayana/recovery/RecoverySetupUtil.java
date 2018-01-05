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

package io.narayana.recovery;

import java.util.Arrays;
import java.util.Properties;

import javax.naming.Context;
import javax.sql.XADataSource;
import javax.transaction.xa.XAResource;

import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import com.arjuna.ats.internal.jdbc.recovery.BasicXARecovery;
import com.arjuna.ats.internal.jdbc.recovery.JDBCXARecovery;
import com.arjuna.ats.internal.jta.recovery.arjunacore.XARecoveryModule;
import com.arjuna.ats.jdbc.common.jdbcPropertyManager;
import com.arjuna.ats.jta.common.jtaPropertyManager;
import com.arjuna.ats.jta.recovery.XAResourceRecovery;

import io.narayana.DriverDirectRecoverable;
import io.narayana.util.TestInitialContextFactory;

/**
 * <p>
 * Utility class which gathers the approaches for recovery setup
 * for jdbc transactional driver.
 * <p>
 * All the settings of recovery (recovery modules, filters, timeouts ) are taken
 * from <code>jbossts-properties.xml</code> descriptor.
 */
public final class RecoverySetupUtil {
    /**
     * <p>
     * Starting recovery manager to be run manually (not periodically)
     * <p>
     * No other settings. This is used with {@link DriverDirectRecoverable}
     * where {@link XAResource} is serialized to object store and after deserializing
     * it has all the necessary info for starting recovery connection.
     * <p>
     * In case of the {@link DriverDirectRecoverable} the information is stored in
     * <code>ds1.*.properties</code>, <code>ds2.*.properties</code> files
     * and that where info for new connection is taken from.
     */
    public static RecoveryManager simpleRecoveryIntialize() {
        RecoveryManager manager = RecoveryManager.manager(RecoveryManager.DIRECT_MANAGEMENT);
        manager.initialize();

        return manager;
    }

    /**
     * <p>
     * Starting recovery manager to be run manually (not periodically)
     * <p>
     * Setting up the programatically property <code>com.arjuna.ats.jta.recovery.XAResourceRecovery</code>
     * (can be done in <code>jbossts-properties.xml</code> or via JVM parameter).<br>
     * <p>
     * Here we intialize XAResourceRecovery which we created for particular database.
     * It creates connection to the specific database and returns {@link XAResource}
     * to check indoubt transactions in database.
     */
    public static RecoveryManager ds1XARecoveryIntialize() {
        jtaPropertyManager.getJTAEnvironmentBean().setXaResourceRecoveryClassNames(Arrays.asList(
                Ds1XAResourceRecovery.class.getName()));

        // direct management means it's not run periodically but we has to manually run recovery scan
        RecoveryManager manager = RecoveryManager.manager(RecoveryManager.DIRECT_MANAGEMENT);
        manager.initialize();

        return manager;
    }

    /**
     * <p>
     * Starting recovery manager to be run manually (not periodically)
     * <p>
     * Setting up the programatically property <code>com.arjuna.ats.jta.recovery.XAResourceRecovery</code>
     * (can be done in <code>jbossts-properties.xml</code> or via JVM parameter).<br>
     * This property defines class implementing {@link XAResourceRecovery}.The {@link XAResourceRecovery} is used
     * during recovery by {@link XARecoveryModule} to get instances of {@link XAResource}.
     * <p>
     * In this case we use {@link JDBCXARecovery} which gets as 'a parameter' an xml file containing jndi name
     * of existing {@link XADataSource} instances which are lookuped and used for recovery.
     */
    public static RecoveryManager jdbcXARecoveryIntialize() {
        jtaPropertyManager.getJTAEnvironmentBean().setXaResourceRecoveryClassNames(Arrays.asList(
            "com.arjuna.ats.internal.jdbc.recovery.JDBCXARecovery;target/classes/recovery-jdbcxa-test1.xml"));

        // our initial context where xa datasources are bound by jdni name
        Properties initProps = new Properties();
        initProps.setProperty(Context.INITIAL_CONTEXT_FACTORY, TestInitialContextFactory.class.getName());
        jdbcPropertyManager.getJDBCEnvironmentBean().setJndiProperties(initProps);

        // direct management means it's not run periodically but we has to manually run recovery scan
        RecoveryManager manager = RecoveryManager.manager(RecoveryManager.DIRECT_MANAGEMENT);
        manager.initialize();

        return manager;
    }

    /**
     * <p>
     * Starting recovery manager to be run manually (not periodically)
     * <p>
     * Setting up the programatically property <code>com.arjuna.ats.jta.recovery.XAResourceRecovery</code>
     * (can be done in <code>jbossts-properties.xml</code> or via JVM parameter).<br>
     * This property defines class implementing {@link XAResourceRecovery}.The {@link XAResourceRecovery} is used
     * during recovery by {@link XARecoveryModule} to get instances of {@link XAResource}.
     * <p>
     * In this case we use {@link BasicXARecovery} which gets as 'a parameter' an xml file containing
     * the same settings that we use during jdbc connection setup. In this case we use jndi name of existing
     * {@link XADataSource} which is later used for recovery.
     */
    public static RecoveryManager basicXARecoveryIntialize() {
        jtaPropertyManager.getJTAEnvironmentBean().setXaResourceRecoveryClassNames(Arrays.asList(
                "com.arjuna.ats.internal.jdbc.recovery.BasicXARecovery;target/classes/recovery-basicxa-test1.xml"));

        // our initial context where xa datasources are bound by jdni name
        Properties initProps = new Properties();
        initProps.setProperty(Context.INITIAL_CONTEXT_FACTORY, TestInitialContextFactory.class.getName());
        jdbcPropertyManager.getJDBCEnvironmentBean().setJndiProperties(initProps);

        // direct management means it's not run periodically but we has to manually run recovery scan
        RecoveryManager manager = RecoveryManager.manager(RecoveryManager.DIRECT_MANAGEMENT);
        manager.initialize();

        return manager;
    }

    /**
     * Running manual recovery scan and then terminate manager.
     * Before another run of recovery you need to <code>RecoveryManager.initialize()</code>.
     */
    public static void runRecovery(RecoveryManager manager) {
        manager.scan();
        manager.terminate();
    }
}
