package org.jboss.narayana.quickstarts.jta;

import jakarta.inject.Inject;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.transaction.Transactional;
import jakarta.transaction.TransactionalException;
import jakarta.transaction.UserTransaction;

/**
 * <p>
 * A class with definition of the
 * {@link Transactional.TxType#MANDATORY} transactional boundary.
 * <p>
 * <p>
 * When any of the methods are called they require transaction
 * context being available on the invocation.
 * If there is no transaction context available on the invocation
 * {@link TransactionalException} is thrown.
 * </p>
 * 
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
@Transactional(Transactional.TxType.MANDATORY)
public class MandatoryCounterManager {

    @Inject
    private Counter counter;

    public boolean isTransactionAvailable() {
        UserTransaction userTransaction = null;
        try {
            userTransaction = (UserTransaction) new InitialContext().lookup("java:/UserTransaction");
        } catch (final NamingException e) {
        }

        return userTransaction != null;
    }

    public int getCounter() {
        return counter.get();
    }

    public void incrementCounter() {
        counter.increment();
    }

}