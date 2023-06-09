package org.jboss.narayana.quickstarts.jta;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.as.arquillian.api.ServerSetup;
import org.jboss.as.arquillian.api.ServerSetupTask;
import org.jboss.as.arquillian.container.ManagementClient;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.online.FailuresAllowedBlock;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.OnlineOptions;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

import jakarta.inject.Inject;
import jakarta.transaction.Transaction;
import jakarta.transaction.TransactionManager;
import jakarta.transaction.TransactionalException;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
@RunWith(Arquillian.class)
@ServerSetup(value = TestCase.ServerTestSetup.class)
public class TestCase {

    public static class ServerTestSetup implements ServerSetupTask {

        @Override
        public void setup(ManagementClient managementClient, String containerId) throws Exception {
            OnlineManagementClient creaper = org.wildfly.extras.creaper.core.ManagementClient
                    .online(OnlineOptions.standalone().wrap(managementClient.getControllerClient()));
            try (FailuresAllowedBlock allowedBlock = creaper.allowFailures()) {
                creaper.execute(
                        "/subsystem=messaging-activemq/server=default/jms-queue=\"test\":add(entries=[java:/queue/test])");
                creaper.execute(
                        "/subsystem=datasources/data-source=QuickstartTestDS:add(connection-url=\"jdbc:h2:mem:quickstart-test\", jndi-name=\"java:jboss/datasources/QuickstartTestDS\", driver-name=h2, user-name=\"sa\", password=\"sa\")");
            }
            new Administration(creaper).reload();
        }

        @Override
        public void tearDown(ManagementClient managementClient, String containerId) throws Exception {
            OnlineManagementClient creaper = org.wildfly.extras.creaper.core.ManagementClient
                    .online(OnlineOptions.standalone().wrap(managementClient.getControllerClient()));
            try (FailuresAllowedBlock allowedBlock = creaper.allowFailures()) {
                creaper.execute("/subsystem=messaging-activemq/server=default/jms-queue=\"test\":remove()");
                creaper.execute("/subsystem=datasources/data-source=QuickstartTestDS:remove()");
            }
        }
    }

    @Inject
    private QuickstartQueue quickstartQueue;

    @Inject
    private QuickstartEntityRepository quickstartEntityRepository;

    @Inject
    private TransactionScopedPojo transactionScopedPojo;

    private TransactionManager transactionManager;

    @Deployment
    public static WebArchive createTestArchive() {
        return ShrinkWrap.create(WebArchive.class, "test.war")
                .addPackages(true, QuickstartEntity.class.getPackage().getName())
                .addAsResource("META-INF/test-persistence.xml", "META-INF/persistence.xml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Before
    public void before() throws NamingException {
        transactionManager = (TransactionManager) new InitialContext().lookup("java:/jboss/TransactionManager");
    }

    @After
    public void after() {
        try {
            transactionManager.rollback();
        } catch (final Throwable t) {
        }
    }

    @Test
    public void testCommit() throws Exception {
        final int entitiesCountBefore = quickstartEntityRepository.findAll().size();

        transactionManager.begin();
        quickstartEntityRepository.save(new QuickstartEntity("test"));
        quickstartQueue.send("testCommit");
        transactionManager.commit();

        Assert.assertEquals(entitiesCountBefore + 1, quickstartEntityRepository.findAll().size());
        Assert.assertEquals("testCommit", quickstartQueue.get());
    }

    @Test
    public void testRollback() throws Exception {
        final int entitiesCountBefore = quickstartEntityRepository.findAll().size();

        transactionManager.begin();
        quickstartEntityRepository.save(new QuickstartEntity("test"));
        quickstartQueue.send("testRollback");
        transactionManager.rollback();

        Assert.assertEquals(entitiesCountBefore, quickstartEntityRepository.findAll().size());
        Assert.assertEquals("", quickstartQueue.get());
    }

    @Test(expected = TransactionalException.class)
    public void testWithoutTransaction() {
        quickstartEntityRepository.save(new QuickstartEntity("test"));
    }

    @Test
    public void testTransactionScoped() throws Exception {
        transactionManager.begin();
        Assert.assertEquals(0, transactionScopedPojo.getValue());
        transactionScopedPojo.setValue(1);

        final Transaction firstTransaction = transactionManager.suspend();

        transactionManager.begin();
        Assert.assertEquals(0, transactionScopedPojo.getValue());
        transactionScopedPojo.setValue(2);

        final Transaction secondTransaction = transactionManager.suspend();

        transactionManager.resume(firstTransaction);
        Assert.assertEquals(1, transactionScopedPojo.getValue());
        transactionManager.commit();

        transactionManager.resume(secondTransaction);
        Assert.assertEquals(2, transactionScopedPojo.getValue());
        transactionManager.commit();
    }

}