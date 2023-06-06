package org.jboss.narayana.quickstarts.ejb;

import java.util.List;

import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.HeuristicMixedException;
import jakarta.transaction.HeuristicRollbackException;
import jakarta.transaction.NotSupportedException;
import jakarta.transaction.RollbackException;
import jakarta.transaction.SystemException;

import org.jboss.logging.Logger;
import org.jboss.narayana.quickstarts.jpa.Customer;
import org.jboss.narayana.quickstarts.jsf.CustomerManagerManagedBean;
import org.jboss.narayana.quickstarts.txoj.CustomerCreationCounter;

@Stateless
public class CustomerManagerEJBImpl implements CustomerManagerEJB {
	private Logger logger = Logger.getLogger(CustomerManagerManagedBean.class
			.getName());

	private CustomerCreationCounter customerCreationCounter = new CustomerCreationCounter();

	@PersistenceContext(name = "my_persistence_ctx")
	EntityManager entityManager;

	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public int createCustomer(String name) throws Exception {
		logger.debug("createCustomer transaction is identified as: "
				+ new InitialContext().lookup("java:/TransactionManager").toString());

		// Can do this first because if there is a duplicate it will be rolled
		// back for us
		customerCreationCounter.incr(1);

		Customer c1 = new Customer();
		c1.setName(name);
		entityManager.persist(c1);

		return c1.getId();
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@SuppressWarnings("unchecked")
	public List<Customer> listCustomers() throws NamingException,
			NotSupportedException, SystemException, SecurityException,
			IllegalStateException, RollbackException, HeuristicMixedException,
			HeuristicRollbackException {
		logger.debug("listCustomers transaction is identified as: "
				+ new InitialContext().lookup("java:/TransactionManager").toString());
		return entityManager.createQuery("select c from Customer c")
				.getResultList();
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public int getCustomerCount() throws Exception {
		logger.debug("getCustomerCount transaction is identified as: "
				+ new InitialContext().lookup("java:/TransactionManager").toString());
		return customerCreationCounter.get();
	}
}