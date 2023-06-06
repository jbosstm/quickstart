package org.jboss.narayana.quickstarts.mongodb.simple;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import org.jboss.narayana.compensations.api.CompensationHandler;

import jakarta.inject.Inject;
import java.net.UnknownHostException;

/**
 * This compensation handler is used to undo a credit operation.
 *
 * @author paul.robinson@redhat.com 09/01/2014
 */
public class UndoCredit implements CompensationHandler {

    //This is the credit data stored during the invocation of the method to be compensated. It gives the compensation handler a hint.
    @Inject
    CreditData creditData;

    //Connection to the MongoDB instance
    @Inject
    DB database;

    @Override
    public void compensate() {

        if (creditData.getToAccount() != null) {
            System.out.println("Undoing credit of '" + creditData.getAmount() + "' to '" + creditData.getToAccount() + "'");

            //Use the creditData to know which account to lookup and by how much to decrement the balance.
            DBCollection accounts = database.getCollection("accounts");
            accounts.update(new BasicDBObject("name", creditData.getToAccount()), new BasicDBObject("$inc", new BasicDBObject("balance", -1 * creditData.getAmount())));
        }
    }
}