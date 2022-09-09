/*
 * JBoss, Home of Professional Open Source
 * Copyright 2016, Red Hat, Inc., and others contributors as indicated
 * by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */
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
