package org.jboss.narayana.quickstart.jca.model;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.jboss.narayana.quickstart.jca.common.AbstractTest;
import org.jboss.narayana.quickstart.jca.exception.DuplicateException;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 *
 */
public final class CustomerDAOTest extends AbstractTest {

    @Test
    public void testGetNextIdWithEmptyDatabase() throws SQLException {
        Assert.assertEquals(1, customerDAO.getNextId());
    }

    @Test
    public void testGetNextIdWithNotEmptyDatabase() throws SQLException, DuplicateException {
        final Customer customer = new Customer(10, "First customer name");
        customerDAO.insert(customer);

        Assert.assertEquals(11, customerDAO.getNextId());
    }

    @Test
    public void testGetAllFromEmptyDatabase() throws SQLException {
        Assert.assertEquals(new ArrayList<Customer>(), customerDAO.getAll());
    }

    @Test
    public void testGetAllFromNotEmptyDatabase() throws SQLException, DuplicateException {
        final Customer customer1 = new Customer(1, "First customer name");
        final Customer customer2 = new Customer(2, "Second customer name");
        customerDAO.insert(customer1);
        customerDAO.insert(customer2);

        final List<Customer> expectedList = new ArrayList<Customer>();
        expectedList.add(customer1);
        expectedList.add(customer2);
        Assert.assertEquals(expectedList, customerDAO.getAll());
    }

    @Test
    public void testInsert() throws SQLException, DuplicateException {
        final Customer customer = new Customer(1, "First customer name");
        customerDAO.insert(customer);
        Assert.assertEquals(customer, customerDAO.get(1));
    }

    @Test(expected = DuplicateException.class)
    public void testInsertDuplicate() throws SQLException, DuplicateException {
        final Customer customer1 = new Customer(1, "First customer name");
        final Customer customer2 = new Customer(2, "First customer name");
        customerDAO.insert(customer1);
        customerDAO.insert(customer2);
    }

    @Test
    public void testUpdate() throws SQLException, DuplicateException {
        final Customer customer1 = new Customer(1, "First customer name");
        final Customer customer2 = new Customer(1, "Second customer name");
        customerDAO.insert(customer1);
        customerDAO.update(customer2);

        Assert.assertEquals(customer2, customerDAO.get(1));
    }

    @Test(expected = DuplicateException.class)
    public void testUpdateDuplicate() throws SQLException, DuplicateException {
        final Customer customer1 = new Customer(1, "First customer name");
        final Customer customer2 = new Customer(2, "Second customer name");
        final Customer customer3 = new Customer(2, "First customer name");
        customerDAO.insert(customer1);
        customerDAO.insert(customer2);
        customerDAO.update(customer3);
    }

    @Test
    public void testDelete() throws SQLException, DuplicateException {
        final Customer customer = new Customer(1, "First customer name");
        customerDAO.insert(customer);
        customerDAO.delete(customer);
        Assert.assertEquals(null, customerDAO.get(1));
    }

}