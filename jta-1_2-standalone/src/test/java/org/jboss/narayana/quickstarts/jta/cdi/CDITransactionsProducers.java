package org.jboss.narayana.quickstarts.jta.cdi;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.interceptor.Interceptor;
import javax.naming.InitialContext;
import jakarta.transaction.TransactionSynchronizationRegistry;

import org.jboss.narayana.quickstarts.jta.CDIBindingTestCase;
import org.jboss.narayana.quickstarts.jta.EventsCounter;

import com.arjuna.ats.jta.common.jtaPropertyManager;

/**
 * <p>
 * This bean produces the {@link TransactionSynchronizationRegistry}.
 * If there is not defined the JNDI binding (the {@link InitialContext} lookup
 * has precedence over the CDI) then the CDI bean is taken for the source
 * for the instance of the txn synchronization registry.
 * </p>
 * <p>
 * This producer defines a way how the {@link TransactionSynchronizationRegistry}
 * is obtained by the application. The {@link Alternative} is used for enabling
 * the bean only in case when CDI binding test case is run.
 * See {@link CDIBindingTestCase#before()}
 * </p>
 * <p>
 * If this producer is not activated then default Narayana implementation
 * of {@link TransactionSynchronizationRegistry} is used.
 * </p>
 */
@Alternative
// priority is needed for the bean being accessible from any CDI scope
@Priority(Interceptor.Priority.APPLICATION+10)
public class CDITransactionsProducers {
    @Inject
    private EventsCounter eventsCounter;

    @Produces
    @ApplicationScoped
    public TransactionSynchronizationRegistry produceTransactionSynchronizationRegistry() {
        eventsCounter.addEvent("@Produces:" + CDITransactionsProducers.class.getSimpleName()
                + "_" +TransactionSynchronizationRegistry.class.getSimpleName());
        return jtaPropertyManager.getJTAEnvironmentBean().getTransactionSynchronizationRegistry();
    }
}