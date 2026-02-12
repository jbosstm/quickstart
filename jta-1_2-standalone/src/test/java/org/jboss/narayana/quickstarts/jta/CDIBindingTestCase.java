package org.jboss.narayana.quickstarts.jta;

import org.jboss.narayana.quickstarts.jta.cdi.CDITransactionsProducers;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.jboss.weld.transaction.spi.TransactionServices;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.transaction.Status;
import jakarta.transaction.SystemException;
import jakarta.transaction.TransactionManager;
import jakarta.transaction.TransactionalException;

/**
 * <p>
 * This test case shows how to initiate the Narayana transaction manager
 * when used with the CDI standalone container.
 * </p>
 * <p>
 * <ul>
 * <li>The Transaction Manager is taken looked up by the {@link TransactionalException}
 * in JNDI and as it's not found it falls back to the Narayana implementation one.
 * See {@link com.arjuna.ats.jta.TransactionManager#transactionManager()}.</li>
 * <li>The synchronization registry is created as the CDI bean
 * by the producer at {@link CDITransactionsProducers}.</li>
 * </ul>
 * </p>
 */
public class CDIBindingTestCase {

    private Weld weld;
    private TransactionManager transactionManager;

    private RequiredCounterManager requiredManager;
    private Counter counter;
    private EventsCounter lifeCycleCounter;

    @BeforeEach
    public void before() throws Exception {
        // Initialize Weld container
        weld = new Weld()
            // producer for synchronization registry activation
            .addBeanClass(CDITransactionsProducers.class)
            .addAlternative(CDITransactionsProducers.class);

        final WeldContainer weldContainer = weld.initialize();

        counter = weldContainer.select(Counter.class).get();
        requiredManager = weldContainer.select(RequiredCounterManager.class).get();
        lifeCycleCounter = weldContainer.select(EventsCounter.class).get();
        lifeCycleCounter.clear();

        transactionManager = weldContainer.select(TransactionManager.class).get();
    }

    @AfterEach
    public void after() throws SystemException {
        // cleaning the transaction state in case of an error
        if(transactionManager.getTransaction() != null
                && transactionManager.getTransaction().getStatus() == Status.STATUS_ACTIVE) {
            try {
                transactionManager.rollback();
            } catch (final Throwable ignored) {
            }
        }

        weld.shutdown();
    }

    @Test
    public void testTransactionScoped() throws Exception {
        transactionManager.begin();
        
        Assertions.assertTrue(lifeCycleCounter.containsEvent("RequiredCounterManager.*Initialized"),
                "Expected the @Initialized scope event to be thrown");

        Assertions.assertEquals(0, counter.get());
        requiredManager.incrementCounter();
        Assertions.assertEquals(1, counter.get());

        transactionManager.rollback();

        Assertions.assertTrue(lifeCycleCounter.containsEvent("RequiredCounterManager.*Destroyed"),
                "Expected the @Destroy scope event to be thrown");

        // verification that TransactionSynchronizationRegistry was taken as the CDI bean from the producer
        Assertions.assertTrue(lifeCycleCounter.containsEvent(CDITransactionsProducers.class.getSimpleName()),
                "Expected TransactionSynchronizationRegistry was created by CDI producer");

        // verification that when TransactionServices are not implemented the observer does not count with 'during'
        Assertions.assertTrue(lifeCycleCounter.containsEvent(TransactionServices.class.getName()),
                "Expected the transactional observers are not working and the event is not deffered"
				        + " and received immediatelly when fired. The transaction was active at time the event was received.");
    }

}