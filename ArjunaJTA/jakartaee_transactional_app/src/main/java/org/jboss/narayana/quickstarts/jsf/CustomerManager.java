package org.jboss.narayana.quickstarts.jsf;

import java.util.List;

import javax.naming.NamingException;
import jakarta.transaction.HeuristicMixedException;
import jakarta.transaction.HeuristicRollbackException;
import jakarta.transaction.NotSupportedException;
import jakarta.transaction.RollbackException;
import jakarta.transaction.SystemException;

import org.jboss.narayana.quickstarts.jpa.Customer;

/**
 * This is the customer manager as seen by the JSF pages. It defines the basic
 * operations required to add and list customers.
 */
public interface CustomerManager {

	/**
	 * Get the list of current customers.
	 * 
	 * @return
	 * @throws SecurityException
	 * @throws IllegalStateException
	 * @throws NamingException
	 * @throws NotSupportedException
	 * @throws SystemException
	 * @throws RollbackException
	 * @throws HeuristicMixedException
	 * @throws HeuristicRollbackException
	 */
	public List<Customer> getCustomers() throws SecurityException,
			IllegalStateException, NamingException, NotSupportedException,
			SystemException, RollbackException, HeuristicMixedException,
			HeuristicRollbackException;

	/**
	 * Add a customer to the database.
	 * 
	 * @param name
	 * @return
	 */
	public String addCustomer(String name);

	/**
	 * Get the total count of customers.
	 * 
	 * @return
	 * @throws Exception
	 */
	public int getCustomerCount() throws Exception;
}