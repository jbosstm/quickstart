package com.arjuna.demo.jts.localbank;

/**
 * This is an in memory implementation of the Bank class used in the JBoss Transactions product trailmap. As it is
 * non-persistent, it is only used to perform the banking operations that all Bank implementations in the trailmap
 * provide, such as creating and accessing an account.
 */
public class Bank
{
    /**
     * Initialize a new in memory bank represenation. As all the accounts are merely stored in a hashtable without a
     * persistent representation, this is all that is required.
     */
    private java.util.Hashtable _accounts = new java.util.Hashtable();

    /**
     * Create a new bank account and store it in the bank. Return a reference to the account.
     *
     * @param name  The name to associate with the account.
     *
     * @return      The <CODE>Account</CODE> reference.
     */
    public Account create_account(String name)
    {
        // Create a new account
        Account acc = new Account(name);

        // Add the new account into the underlying data structure
        _accounts.put(name, acc);

        // Return the account object.
        return acc;
    }

    /**
     * Gain access to the bank account reference name of "name".
     *
     * @param name                  The name of the account to access.
     * @return                      The suitable account reference.
     *
     * @throws NotExistingAccount   If the account did not exist.
     */
    public Account get_account(String name) throws NotExistingAccount
    {
        // Gain a local reference to the account
        Account acc = (Account) _accounts.get(name);

        // If the account does not exist throw an exception to the user
        if (acc == null)
            throw new NotExistingAccount("The Account requested does not exist");

        // Return the reference
        return acc;
    }
}