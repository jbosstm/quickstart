/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
 * See the copyright.txt in the distribution for a full listing
 * of individual contributors.
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
 *
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
package org.jboss.narayana.quickstarts.mongodb.simple;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.narayana.compensations.api.TransactionCompensatedException;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;

import jakarta.inject.Inject;
import java.io.File;

@RunWith(Arquillian.class)
public class BankingServiceTest {

    private DBCollection accounts;

    @Inject
    BankingService bankingService;

    @Deployment
    public static WebArchive createTestArchive() {

        //Use 'Shrinkwrap Resolver' to include the mongodb java driver in the deployment
        File lib = Maven.resolver().resolve("org.mongodb:mongo-java-driver:3.12.11").withoutTransitivity().asSingleFile();

        WebArchive archive = ShrinkWrap.create(WebArchive.class, "test.war")
                .addPackages(true, BankingService.class.getPackage().getName())
                .addAsWebInfResource(new StringAsset("<beans bean-discovery-mode=\"all\"></beans>"), "beans.xml")
                .addAsLibraries(lib);

        return archive;
    }


    /**
     * Setup the initial test data. Give both accounts 'A' and 'B' £1000
     *
     * @throws Exception
     */
    @Before
    public void resetAccountData() throws Exception {
        MongoClient mongo = new MongoClient("localhost", 27017);
        DB database = mongo.getDB("test");

        database.getCollection("accounts").drop();
        accounts = database.getCollection("accounts");

        accounts.insert(new BasicDBObject("name", "A").append("balance", 1000.0));
        accounts.insert(new BasicDBObject("name", "B").append("balance", 1000.0));
    }

    /**
     * Transfer £100 from A to B and assert that it was successful.
     *
     * @throws Exception
     */
    @Test
    public void testSuccess() throws Exception {

        bankingService.transferMoney("A", "B", 100.0);
        assertBalance("A", 900.0);
        assertBalance("B", 1100.0);
    }

    /**
     * Attempt to transfer £600 from A to B. The banking service will fail this transfer due to the amount being
     * above the transfer limit.
     *
     * The test asserts that both balances are set to £1000 after the transaction fails.
     *
     * @throws Exception
     */
    @Test
    public void testFailure() throws Exception {

        //Initiate a 'high value' transfer that will fail
        try {
            bankingService.transferMoney("A", "B", 600.0);
            Assert.fail("Expected a TransactionCompensatedException to be thrown");
        } catch (TransactionCompensatedException e) {
            //expected
        }
        assertBalance("A", 1000.0);
        assertBalance("B", 1000.0);
    }

    /**
     * Simple helper method that requests a user's account document and asserts that the balance is as expected.
     *
     * @param account The account name, used to lookup the right account document.
     * @param expectedBalance The expected balance
     */
    private void assertBalance(String account, Double expectedBalance) {
        DBObject accountDoc = accounts.findOne(new BasicDBObject("name", account));
        Double actualBalance = (Double) accountDoc.get("balance");
        Assert.assertEquals("Balance is not as expected. Got '" + actualBalance + "', expected: '" + expectedBalance + "'", expectedBalance, actualBalance, 0);
    }
}
