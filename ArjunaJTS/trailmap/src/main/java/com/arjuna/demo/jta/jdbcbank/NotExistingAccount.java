package com.arjuna.demo.jta.jdbcbank;

/**
 * This exception class should be used if the bank account requested by a user cannot be found.
 */
public class NotExistingAccount extends Exception
{
    /**
     * Constructs a new instance of NotSupportedException using a message for explanation.
     *
     * @param message The reason that the exception has been raised.
     */
    public NotExistingAccount(String message)
    {
        super(message);
    }

}