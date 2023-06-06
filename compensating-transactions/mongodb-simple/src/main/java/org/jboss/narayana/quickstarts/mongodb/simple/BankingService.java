package org.jboss.narayana.quickstarts.mongodb.simple;

import org.jboss.narayana.compensations.api.Compensatable;

import jakarta.inject.Inject;

/**
 * This service is responsible for transferring money from one account to another.
 *
 * @author paul.robinson@redhat.com 09/01/2014
 */
public class BankingService {

    @Inject
    AccountManager accountManager;

    /**
     *
     * This method transfers money from one account to another. It invokes two operations which each update a
     * separate MongoDB document. These two operations need to be atomic, so the method is annotated with @Compensatable to
     * ensure that it is invoked with a compensating transaction. If this thread is already running in a compensating
     * transaction, it will be re-used. Otherwise, a new Compensating transaction will be begun.
     *
     * A RuntimeExcpetion thrown from this method will cause the compensating transaction to fail and compensation handlers
     * to be invoked for any completed work.
     *
     * @param fromAccount The account to transfer the money from.
     * @param toAccount The account to transfer the money to.
     * @param amount The amount to transfer.
     */
    @Compensatable
    public void transferMoney(String fromAccount, String toAccount, Double amount) {

        accountManager.debitAccount(fromAccount, amount);
        accountManager.creditAccount(toAccount, amount);
    }

}