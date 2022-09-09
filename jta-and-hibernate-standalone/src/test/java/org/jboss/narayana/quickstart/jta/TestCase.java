/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.narayana.quickstart.jta;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import javax.naming.InitialContext;
import javax.sql.DataSource;
import jakarta.transaction.TransactionManager;

import com.arjuna.ats.arjuna.common.recoveryPropertyManager;
import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import com.arjuna.ats.internal.jta.recovery.arjunacore.XARecoveryModule;
import org.jboss.byteman.contrib.bmunit.BMRule;
import org.jboss.byteman.contrib.bmunit.BMUnitRunner;
import org.jboss.weld.environment.se.Weld;
import org.jnp.server.NamingBeanImpl;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.arjuna.ats.jta.utils.JNDIManager;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
@RunWith(BMUnitRunner.class)
public class TestCase {

    /**
     * JNDI server.
     */
    private static final NamingBeanImpl NAMING_BEAN = new NamingBeanImpl();

    /**
     * Transaction manager for transaction demarcation.
     */
    private static TransactionManager transactionManager;

    /**
     * Repository to create test entities.
     */
    private static QuickstartEntityRepository quickstartEntityRepository;

    /**
     * CDI container.
     */
    private Weld weld;

    @BeforeClass
    public static void beforeClass() throws Exception {
        NAMING_BEAN.start();
        JNDIManager.bindJTAImplementation();
        new InitialContext().bind(TransactionalConnectionProvider.DATASOURCE_JNDI, QuickstartApplication.getDataSource());
        recoveryPropertyManager.getRecoveryEnvironmentBean()
                .setRecoveryModuleClassNames(QuickstartApplication.getRecoveryModuleClassNames());
        recoveryPropertyManager.getRecoveryEnvironmentBean().setRecoveryBackoffPeriod(1);
        QuickstartApplication.setObjectStoreDir();
        RecoveryManager.manager(RecoveryManager.DIRECT_MANAGEMENT).getModules().stream()
                .filter(m -> m instanceof XARecoveryModule)
                .forEach(m -> ((XARecoveryModule) m).addXAResourceRecoveryHelper(new DummyXAResourceRecoveryHelper()));
    }

    @AfterClass
    public static void afterClass() {
        // Stop JNDI server
        NAMING_BEAN.stop();
    }

    @Before
    public void before() throws Exception {
        weld = new Weld();
        transactionManager = InitialContext.doLookup("java:/TransactionManager");
        quickstartEntityRepository = weld.initialize().instance().select(QuickstartEntityRepository.class).get();
        quickstartEntityRepository.clear();
    }

    @After
    public void after() {
        try {
            transactionManager.rollback();
        } catch (Throwable t) {
        }

        weld.shutdown();
    }

    /**
     * Adds two entries to the database and commits the transaction. At the end of the test two entries should be in the
     * database.
     *
     * @throws Exception
     */
    @Test
    public void testCommit() throws Exception {
        QuickstartEntity firstEntity = getNewEntity();
        QuickstartEntity secondEntity = getNewEntity();
        transactionManager.begin();
        quickstartEntityRepository.save(firstEntity);
        quickstartEntityRepository.save(secondEntity);
        transactionManager.commit();
        assertEntities(firstEntity, secondEntity);
    }

    /**
     * Adds two entries to the database and rolls back the transaction. At the end of the test no entries should be in the
     * database.
     * 
     * @throws Exception
     */
    @Test
    public void testRollback() throws Exception {
        QuickstartEntity firstEntity = getNewEntity();
        QuickstartEntity secondEntity = getNewEntity();
        transactionManager.begin();
        quickstartEntityRepository.save(firstEntity);
        quickstartEntityRepository.save(secondEntity);
        transactionManager.rollback();
        assertEntities();
    }

    /**
     * Enlists DummyXAResource and adds one entry to the database. After preparing both resources system crash is simulated.
     * Straight after that no entries should be in the database. However, after executing a recovery scan one entry should be in
     * the database.
     * 
     * @throws Exception
     */
    @Test
    @BMRule(name = "Fail after first commit", targetLocation = "ENTRY", targetClass = "com.arjuna.ats.arjuna.coordinator.BasicAction", targetMethod = "phase2Commit", helper = "org.jboss.narayana.quickstart.jta.BytemanHelper", action = "incrementCommitsCounter(); failFirstCommit($0.get_uid());")
    public void testCrashBeforeCommit() throws Exception {
        QuickstartEntity entity = getNewEntity();

        transactionManager.begin();
        transactionManager.getTransaction().enlistResource(new DummyXAResource());
        quickstartEntityRepository.save(entity);

        try {
            transactionManager.commit();
            fail("QuickstartRuntimeException expected");
        } catch (Throwable t) {
            assertTrue("Exception was: " + t.getClass() + ":" + t.getMessage(), t.getCause() instanceof QuickstartRuntimeException);
        }

        assertEntities();
        RecoveryManager.manager(RecoveryManager.DIRECT_MANAGEMENT).scan();
        assertEntities(entity);
    }

    private void assertEntities(QuickstartEntity... expected) throws Exception {
        assertEquals(Arrays.asList(expected), getEntitiesFromTheDatabase());
    }

    private List<QuickstartEntity> getEntitiesFromTheDatabase() throws Exception {
        DataSource dataSource = InitialContext.doLookup("java:/quickstartDataSource");
        Connection connection = dataSource.getConnection(TransactionalConnectionProvider.USERNAME,
                TransactionalConnectionProvider.PASSWORD);
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT `value2` FROM `QuickstartEntity`");
        List<QuickstartEntity> entities = new LinkedList<>();
        while (resultSet.next()) {
            entities.add(new QuickstartEntity(resultSet.getString("value2")));
        }
        resultSet.close();
        statement.close();
        connection.close();
        return entities;
    }

    private QuickstartEntity getNewEntity() {
        return new QuickstartEntity("Test entity at " + LocalTime.now());
    }

}
