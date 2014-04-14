package org.jboss.narayana.quickstarts.mongodb.simple;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import org.jboss.narayana.compensations.api.CompensationHandler;

import javax.inject.Inject;
import java.net.UnknownHostException;

/**
 * This compensation handler is used to undo a debit operation.
 *
 * @author paul.robinson@redhat.com 09/01/2014
 */
public class UndoDebit implements CompensationHandler {

    //This is the credit data stored during the invocation of the method to be compensated. It gives the compensation handler a hint.
    @Inject
    DebitData debitData;

    //Connection to the MongoDB instance
    @Inject
    DB database;

    @Override
    public void compensate() {

        System.out.println("Undoing debit of '" + debitData.getAmount() + "' from '" + debitData.getFromAccount() + "'");

        //Use the debitData to know which account to lookup and by how much to increment the balance.
        DBCollection accounts = database.getCollection("accounts");
        accounts.update(new BasicDBObject("name", debitData.getFromAccount()), new BasicDBObject("$inc", new BasicDBObject("balance", debitData.getAmount())));
    }
}
