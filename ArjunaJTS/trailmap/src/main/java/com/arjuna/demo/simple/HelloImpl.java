package com.arjuna.demo.simple;


/**
 * This class is created and exposed as a remote object by the <CODE>HelloServer</CODE>. It is a CORBA object that
 * provides transaction-aware operations, in this case a single operation print_hello().
 *
 * If the print_hello command is issued within the scope of a transaction then the call will be executed within a
 * transaction.
 */
public class HelloImpl extends HelloPOA
{
    /**
     * This method simply displays a greeting on the console.
     */
    public void print_hello()
    {
        System.out.println("Hello - called within a scope of a transaction");
    }
}