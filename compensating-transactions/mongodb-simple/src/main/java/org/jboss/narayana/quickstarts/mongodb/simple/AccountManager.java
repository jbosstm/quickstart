package org.jboss.narayana.quickstarts.mongodb.simple;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import org.jboss.narayana.compensations.api.CompensationManager;
import org.jboss.narayana.compensations.api.TxCompensate;

import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.inject.Inject;
import java.net.UnknownHostException;

/**
 * This class offers banking services for crediting or debiting an account.
 *
 * Both business logic methods are annotated with a compensation handler. This is so that the work they undertake can later be
 * undone, should the transaction fail. Each method also makes use of a CompensationScoped bean to store information
 * about their work. This information is made available to the compensation handler if it is needed.
 *
 * @author paul.robinson@redhat.com 09/01/2014
 */
public class AccountManager {

    @Inject
    DB database;

    //The compensation manager provides a way for the business logic to interact with the current compensating transaction.
    @Inject
    private CompensationManager compensationManager;

    //A @CompensationScoped POJO used to store compensation state
    @Inject
    CreditData creditData;

    //A @CompensationScoped POJO used to store compensation state
    @Inject
    DebitData debitData;


    public AccountManager() {

    }


    /**
     * This method credits a specified account by a specified amount.
     *
     * It is annotated with @TxCompensate. This annotation specifies the implementation of a compensation handler that
     * is to be invoked if the work done in this method needs to be compensated later.
     *
     * @param account The account to credit
     * @param amount The amount to credit the balance by.
     */
    @TxCompensate(UndoCredit.class)
    public void creditAccount(String account, Double amount) {

        //High value transfers (over 500) are not allowed with this service
        if (amount > 500) {
            //Mark the current transaction as 'compensateOnly'. This ensures that the transaction will fail.
            compensationManager.setCompensateOnly();
            return;
        }

        //Set the compensation state. This will be required if the compensation handler is invoked.
        creditData.setToAccount(account);
        creditData.setAmount(amount);

        //Update the account document in MongoDB.
        DBCollection accounts = database.getCollection("accounts");
        accounts.update(new BasicDBObject("name", account), new BasicDBObject("$inc", new BasicDBObject("balance", amount)));

    }

    /**
     * This method debits a specified account by a specified amount.
     *
     * It is annotated with @TxCompensate. This annotation specifies the implementation of a compensation handler that
     * is to be invoked if the work done in this method needs to be compensated later.
     *
     * @param account The account to debit
     * @param amount The amount to debit the balance by.
     */
    @TxCompensate(UndoDebit.class)
    public void debitAccount(String account, Double amount) {

        //Set the compensation state. This will be required if the compensation handler is invoked.
        debitData.setFromAccount(account);
        debitData.setAmount(amount);

        //Update the account document in MongoDB.
        DBCollection accounts = database.getCollection("accounts");
        accounts.update(new BasicDBObject("name", account), new BasicDBObject("$inc", new BasicDBObject("balance", -1 * amount)));

    }

}