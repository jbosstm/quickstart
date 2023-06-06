package org.jboss.narayana.quickstart.jca.xa;

import java.sql.SQLException;

import jakarta.transaction.TransactionManager;

import org.jboss.narayana.quickstart.jca.common.AbstractTest;
import org.jboss.narayana.quickstart.jca.model.Customer;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 *
 */
public class XAResourcesTest extends AbstractTest {

    private DummyXAResource dummyXAResource;

    private TransactionManager transactionManager;

    @Before
    public void before() throws Exception {
        transactionManager = com.arjuna.ats.jta.TransactionManager.transactionManager();
        transactionManager.begin();
        super.before();
        dummyXAResource = new DummyXAResource();
        transactionManager.getTransaction().enlistResource(dummyXAResource);
    }

    @After
    public void after() throws SQLException {
        try {
            transactionManager.rollback();
        } catch (Exception e) {
        }
        super.after();
    }

    @Test
    public void testCommitWithBothResources() throws Exception {
        final Customer customer = new Customer(1, "First customer name");
        customerDAO.insert(customer);

        final int counterBefore = dummyXAResource.getCommitedTransactionsCounter();

        transactionManager.commit();

        final int counterAfter = dummyXAResource.getCommitedTransactionsCounter();

        Assert.assertEquals(counterBefore + 1, counterAfter);
        Assert.assertEquals(customer, customerDAO.get(1));
    }

    @Test
    public void testRollbackWithBothResources() throws Exception {
        final Customer customer = new Customer(1, "First customer name");
        customerDAO.insert(customer);

        final int counterBefore = dummyXAResource.getCommitedTransactionsCounter();

        transactionManager.rollback();

        final int counterAfter = dummyXAResource.getCommitedTransactionsCounter();

        Assert.assertEquals(counterBefore, counterAfter);
        Assert.assertEquals(null, customerDAO.get(1));
    }

}