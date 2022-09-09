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
package org.jboss.narayana.quickstarts.jta;

import jakarta.enterprise.context.Destroyed;
import jakarta.enterprise.context.Initialized;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.TransactionPhase;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;
import jakarta.transaction.Status;
import jakarta.transaction.SystemException;
import jakarta.transaction.TransactionManager;
import jakarta.transaction.TransactionScoped;
import jakarta.transaction.Transactional;
import jakarta.transaction.UserTransaction;

import org.jboss.logging.Logger;
import org.jboss.weld.transaction.spi.TransactionServices;

/**
 * <p>
 * A class with definition of the
 * {@link Transactional.TxType#REQUIRED} transactional boundary
 * for one particular method {@link #isTransactionAvailable()}.
 * </p>
 * <p>
 * If the method is invoked with a transactional context being
 * available the method joins the context.
 * If there is no context available a new transactional context
 * is created (a new transaction is started) before the method
 * code is executed.
 * </p>
 * <p>
 * The class demonstrates the usage of the {@link TransactionScoped} events.
 * A method can observe the {@link Initialized} and {@link Destroyed} events.
 * </p>
 * 
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class RequiredCounterManager {
    private static final Logger LOG = Logger.getLogger(RequiredCounterManager.class);

    @Inject
    private Counter counter;

    @Inject
    private EventsCounter lifeCycle;

    @Inject
    private TransactionManager transactionManager;

    // available only when Weld TransactionServices are implemented
    // @Inject
    // private UserTransaction userTransaction;

    @Inject @Any
    private Event<Counter> counterEvent;

    @Transactional
    public boolean isTransactionAvailable() {
        try {
            return transactionManager.getTransaction().getStatus() == Status.STATUS_ACTIVE;
        } catch (SystemException se) {
            throw new IllegalStateException("Transaction manager " + transactionManager
                    + " is not capable to provide transaction status");
        }
    }

    public int getCounter() {
        return counter.get();
    }

    @Transactional
    public void incrementCounter() {
        counter.increment();
        counterEvent.fire(counter);
    }

    void transactionScopeActivated(@Observes @Initialized(TransactionScoped.class) final Object event, final BeanManager beanManager) {
        lifeCycle.addEvent(this.getClass().getSimpleName() + "_" + Initialized.class.getSimpleName());
    }

    void transactionScopeDestroyed(@Observes @Destroyed(TransactionScoped.class) final Object event, final BeanManager beanManager) {
        try {
            lifeCycle.addEvent(this.getClass().getSimpleName() + "_" + Destroyed.class.getSimpleName());
        } catch (Exception e) {
            LOG.trace("This is the expected situation."
                    + "The context was destroyed the @Transactional scope is not available at this time.");
        }
    }

    /**
     * If the transaction observer is correctly configured then this observer fails to be executed.
     * The {@link Counter} is in scope {@link Transactional} and thus in {@link TransactionPhase#AFTER_COMPLETION}
     * is not active anymore.<br/>
     * If the transaction observers are not working then the event is delivered immediately - it's not deferred
     * at time of the transaction was completed - then transaction is still active and the {@link Counter}
     * can be used. 
     */
    void transactionObserverAfterCompletion(@Observes(during=TransactionPhase.AFTER_COMPLETION) Counter counterValue) {
        lifeCycle.addEvent(counterValue.get() + " should fail. If not then the " + TransactionServices.class + " is not implemented.");
    }
}
