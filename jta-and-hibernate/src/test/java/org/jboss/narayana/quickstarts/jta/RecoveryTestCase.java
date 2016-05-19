package org.jboss.narayana.quickstarts.jta;

import com.arjuna.ats.jta.TransactionManager;
import com.arjuna.ats.jta.utils.JNDIManager;
import org.h2.jdbcx.JdbcDataSource;
import org.jboss.byteman.contrib.bmunit.BMRule;
import org.jboss.byteman.contrib.bmunit.BMUnitRunner;
import org.jboss.narayana.quickstarts.jta.jpa.TestEntity;
import org.jboss.narayana.quickstarts.jta.jpa.TestEntityRepository;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.jnp.server.NamingBeanImpl;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.naming.InitialContext;
import javax.transaction.Transaction;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
@RunWith(BMUnitRunner.class)
public class RecoveryTestCase {

    @Mock
    private XAResource firstResource;

    @Mock
    private XAResource secondResource;

    private static final NamingBeanImpl NAMING_BEAN = new NamingBeanImpl();

    private javax.transaction.TransactionManager transactionManager;

    private RequiredCounterManager requiredCounterManager;

    private MandatoryCounterManager mandatoryCounterManager;

    private TestEntityRepository testEntityRepository;

    private Weld weld;

    @BeforeClass
    public static void beforeClass() throws Exception {
        // Start JNDI server
        NAMING_BEAN.start();

        // Bind JTA implementation with default names
        JNDIManager.bindJTAImplementation();

        // Setup datasource
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:test;MODE=Oracle;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
        dataSource.setUser("sa");
        dataSource.setPassword("");
        new InitialContext().bind("java:/quickstartDataSource", dataSource);
    }

    @AfterClass
    public static void afterClass() {
        NAMING_BEAN.stop();
    }

    @Before
    public void before() throws Exception {
        MockitoAnnotations.initMocks(this);

        transactionManager = (javax.transaction.TransactionManager) new InitialContext().lookup("java:/TransactionManager");

        // Initialize Weld container
        weld = new Weld();
        final WeldContainer weldContainer = weld.initialize();

        // Bootstrap the beans
        requiredCounterManager = weldContainer.instance().select(RequiredCounterManager.class).get();
        mandatoryCounterManager = weldContainer.instance().select(MandatoryCounterManager.class).get();
        testEntityRepository = weldContainer.instance().select(TestEntityRepository.class).get();

        testEntityRepository.clear();
    }

    @After
    public void after() {
        try {
            transactionManager.rollback();
        } catch (final Throwable t) {
        }

        weld.shutdown();
    }

    @Test
    @BMRule(name = "Fail before commit", targetClass = "com.arjuna.ats.arjuna.coordinator.BasicAction",
            targetMethod = "phase2Commit", targetLocation = "ENTRY", helper = "org.jboss.narayana.quickstarts.jta.BytemanHelper",
            action = "incrementCommitsCounter(); failFirstCommit($0.get_uid());")
    public void testCrashBeforeCommit() throws Exception {
        when(firstResource.prepare(any(Xid.class))).thenReturn(XAResource.XA_OK);
        when(secondResource.prepare(any(Xid.class))).thenReturn(XAResource.XA_OK);

        assertEquals(0, testEntityRepository.findAll().size());

        transactionManager.begin();
        transactionManager.getTransaction().enlistResource(firstResource);
        testEntityRepository.save(new TestEntity("test1"));
        Transaction suspendedTransaction = transactionManager.suspend();
        assertEquals(0, testEntityRepository.findAll().size());
        transactionManager.resume(suspendedTransaction);

        try {
            TransactionManager.transactionManager().commit();
            fail("TestRuntimeException expected");
        } catch (Throwable t) {
            assertTrue(t.getCause() instanceof TestRuntimeException);
        }

        assertEquals(0, testEntityRepository.findAll().size());
    }

}
