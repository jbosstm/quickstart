package org.jboss.narayana.quickstarts.ejb;

import java.util.List;

import javax.naming.NamingException;
import jakarta.transaction.HeuristicMixedException;
import jakarta.transaction.HeuristicRollbackException;
import jakarta.transaction.NotSupportedException;
import jakarta.transaction.RollbackException;
import jakarta.transaction.SystemException;

import org.jboss.narayana.quickstarts.jpa.Customer;

/**
 * A simple example to show some transactional business logic.
 */
public interface CustomerManagerEJB {
	/**
	 * The business logic.
	 * 
	 * @return
	 * @throws NamingException
	 * @throws Exception
	 */
	public int createCustomer(String name) throws NamingException, Exception;

	/**
	 * List all the customers.
	 * 
	 * @return
	 * @throws NamingException
	 * @throws NotSupportedException
	 * @throws SystemException
	 * @throws SecurityException
	 * @throws IllegalStateException
	 * @throws RollbackException
	 * @throws HeuristicMixedException
	 * @throws HeuristicRollbackException
	 */
	public List<Customer> listCustomers() throws NamingException,
			NotSupportedException, SystemException, SecurityException,
			IllegalStateException, RollbackException, HeuristicMixedException,
			HeuristicRollbackException;

	/**
	 * Get the total count of customers.
	 * 
	 * @return
	 * @throws Exception
	 */
	public int getCustomerCount() throws Exception;
}