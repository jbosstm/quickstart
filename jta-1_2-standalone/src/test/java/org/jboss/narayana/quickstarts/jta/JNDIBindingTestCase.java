package org.jboss.narayana.quickstarts.jta;

import static org.junit.jupiter.api.Assertions.assertThrows;

import javax.naming.Context;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.jnp.server.NamingBeanImpl;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.arjuna.ats.jta.utils.JNDIManager;

import jakarta.transaction.Status;
import jakarta.transaction.SystemException;
import jakarta.transaction.Transaction;
import jakarta.transaction.TransactionManager;
import jakarta.transaction.TransactionSynchronizationRegistry;
import jakarta.transaction.TransactionalException;
import jakarta.transaction.UserTransaction;

/**
 * <p>
 * This test case shows how to initiate the Narayana transaction manager
 * when used with the CDI standalone container.
 * </p>
 * <p>
 * Narayana configuration can be provided by the <code>jbossts-properties.xml</code>
 * and/or (re)defined programatically setting up properties to particular
 * configuration bean. You can see this in {@link #beforeClass()}.
 * </p>
 * <p>
 * The standalone CDI container is <a href="http://weld.cdi-spec.org">Weld</a>
 * is started in {@link #before()}. The JTA CDI extension needs
 * to find out the implementation of the {@link TransactionManager},
 * {@link TransactionSynchronizationRegistry} and {@link UserTransaction}
 * which is in this case bound to JNDI {@link JNDIManager#bindJTAImplementation()}.
 * The JTA CDI extension finds out the configured instances with JNDI lookup.
 * </p>
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class JNDIBindingTestCase {

    private static final NamingBeanImpl NAMING_BEAN = new NamingBeanImpl();

    private Weld weld;
    private TransactionManager transactionManager;

    private RequiredCounterManager requiredCounterManager;
    private MandatoryCounterManager mandatoryCounterManager;
    private EventsCounter lifeCycleCounter;


    @BeforeAll
    public static void beforeClass() throws Exception {
        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.jnp.interfaces.NamingContextFactory");
        System.setProperty(Context.URL_PKG_PREFIXES, "org.jboss.naming:org.jnp.interfaces");

        // Start JNDI server
        NAMING_BEAN.start();

        // Bind JTA implementation with default names
        JNDIManager.bindJTAImplementation();
    }

    @AfterAll
    public static void afterClass() {
        NAMING_BEAN.stop();

        System.clearProperty(Context.INITIAL_CONTEXT_FACTORY);
        System.clearProperty(Context.URL_PKG_PREFIXES);
    }

    @BeforeEach
    public void before() throws Exception {
        // Initialize Weld container
        weld = new Weld();
        final WeldContainer weldContainer = weld.initialize();

        // Bootstrap the beans
        requiredCounterManager = weldContainer.select(RequiredCounterManager.class).get();
        mandatoryCounterManager = weldContainer.select(MandatoryCounterManager.class).get();

        lifeCycleCounter = weldContainer.select(EventsCounter.class).get();
        lifeCycleCounter.clear();

        transactionManager = weldContainer.select(TransactionManager.class).get();
    }

    @AfterEach
    public void after() throws SystemException {
        // cleaning the transaction state in case of an error
        if(transactionManager.getTransaction()!=null
                && transactionManager.getTransaction().getStatus() == Status.STATUS_ACTIVE) {
            try {
                transactionManager.rollback();
            } catch (final Throwable ignored) {
            }
        }

        weld.shutdown();
    }

    @Test
    public void testRequiredTransactionWithExistingTransaction() throws Exception {
        transactionManager.begin();
        Assertions.assertTrue(requiredCounterManager.isTransactionAvailable());
        transactionManager.rollback();
    }

    @Test
    public void testRequiredTransactionWithoutExistingTransaction() {
        Assertions.assertTrue(requiredCounterManager.isTransactionAvailable());

        Assertions.assertTrue(lifeCycleCounter.containsEvent("RequiredCounterManager.*Initialized"));
        Assertions.assertTrue(lifeCycleCounter.containsEvent("RequiredCounterManager.*Destroyed"));
    }

    @Test
    public void testMandatoryTransactionWithExistingTransaction() throws Exception {
        transactionManager.begin();
        Assertions.assertTrue(mandatoryCounterManager.isTransactionAvailable());
        transactionManager.rollback();
    }

    @Test
    public void testMandatoryTransactionWithoutExistingTransaction() {
    	assertThrows(TransactionalException.class, () -> {
    		mandatoryCounterManager.isTransactionAvailable();
    	});
    }

    @Test
    public void testTransactionScoped() throws Exception {
        transactionManager.begin();
        Assertions.assertEquals(0, requiredCounterManager.getCounter());
        Assertions.assertEquals(0, mandatoryCounterManager.getCounter());
        requiredCounterManager.incrementCounter();
        Assertions.assertEquals(1, requiredCounterManager.getCounter());
        Assertions.assertEquals(1, mandatoryCounterManager.getCounter());

        Assertions.assertTrue(lifeCycleCounter.containsEvent("RequiredCounterManager.*Initialized"),
                "Expected the @Initialized scope event to be thrown");

        final Transaction suspendedTransaction = transactionManager.suspend();

        transactionManager.begin();
        Assertions.assertEquals(0, requiredCounterManager.getCounter());
        Assertions.assertEquals(0, mandatoryCounterManager.getCounter());
        mandatoryCounterManager.incrementCounter();
        Assertions.assertEquals(1, requiredCounterManager.getCounter());
        Assertions.assertEquals(1, mandatoryCounterManager.getCounter());

        transactionManager.rollback();
        transactionManager.resume(suspendedTransaction);
        transactionManager.rollback();

        Assertions.assertTrue(lifeCycleCounter.containsEvent("RequiredCounterManager.*Destroyed"),
                "Expected the @Destroy scope event to be thrown");
    }

}