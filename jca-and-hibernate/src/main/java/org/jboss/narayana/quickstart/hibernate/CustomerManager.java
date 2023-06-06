package org.jboss.narayana.quickstart.hibernate;

import org.jboss.logging.Logger;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.transaction.RollbackException;
import jakarta.transaction.TransactionManager;
import java.util.List;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class CustomerManager {

    private static final Logger LOG = Logger.getLogger(CustomerManager.class);

    private final EntityManagerFactory entityManagerFactory;

    private final TransactionManager transactionManager;

    public CustomerManager(final EntityManagerFactory entityManagerFactory) throws NamingException {
        this.entityManagerFactory = entityManagerFactory;
        transactionManager = (TransactionManager) new InitialContext().lookup("java:/TransactionManager");
    }

    /**
     * Returns all customers stored in the database.
     *
     * @return List of all customers.
     * @throws Exception
     */
    public List<Customer> getCustomers() throws Exception {
        if (LOG.isTraceEnabled()) {
            LOG.trace("CustomerManager - getting a list of all customers.");
        }

        transactionManager.begin();
        final EntityManager entityManager = entityManagerFactory.createEntityManager();
        final List<Customer> customers = entityManager.createQuery("from Customer").getResultList();
        transactionManager.commit();
        entityManager.close();

        if (LOG.isTraceEnabled()) {
            LOG.trace("CustomerManager - customers list received: " + customers + ".");
        }

        return customers;
    }

    /**
     * Adds new customer to the database.
     *
     * Only customers with the unique names are allowed.
     *
     * @param name Name of the new customer.
     * @return True if the customer was added successfully and False otherwise.
     * @throws Exception
     */
    public boolean addCustomer(final String name) throws Exception {
        if (LOG.isTraceEnabled()) {
            LOG.trace("CustomerManager - adding new customer with name = " + name + ".");
        }

        transactionManager.begin();

        // Enlisting additional XA resource, in order to see two phase commit in action
        transactionManager.getTransaction().enlistResource(new DummyXAResource());

        final EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.persist(new Customer(name));

        try {
            transactionManager.commit();
            entityManager.close();
        } catch (final RollbackException e) {
            if (LOG.isTraceEnabled()) {
                LOG.trace("CustomerManager - failed to add a new customer. Transaction was rolled back.");
            }

            return false;
        }

        if (LOG.isTraceEnabled()) {
            LOG.trace("CustomerManager - new customer was added successfully.");
        }

        return true;
    }

    /**
     * Removes all customers from the database.
     *
     * @throws Exception
     */
    public void clear() throws Exception {
        if (LOG.isTraceEnabled()) {
            LOG.trace("CustomerManager - removing all customers from the database.");
        }

        transactionManager.begin();
        final EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.createQuery("DELETE from Customer").executeUpdate();
        transactionManager.commit();
        entityManager.close();

        if (LOG.isTraceEnabled()) {
            LOG.trace("CustomerManager - all customers were successfully removed.");
        }
    }

}