package com.arjuna.demo.jts.localbank;

import com.arjuna.ats.jts.OTSManager;
import com.arjuna.orbportability.OA;
import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.RootOA;
import org.omg.CosTransactions.Current;

import java.io.IOException;

/**
 * The <CODE>BankClient</CODE> application is an interactive CLI that allows the user of the JBoss Transactions product
 * to manipulate a database backed bank under transactional control.
 */
public class BankClient
{
    /**
     * A reference to the bank to invoke banking operations upon.
     */
    private Bank _bank;

    /**
     * Create a new BankClient indicating the Bank to execute updates and queries upon.
     *
     * @param bank  A reference to the bank to update.
     */
    private BankClient(Bank bank)
    {
        // Store this reference for calls to be executed upon
        _bank = bank;
    }

    /**
     * Display menu and return the selected operation.
     *
     * @return The users choice of command.
     */
    private static int menu()
    {
        System.out.println("");
        System.out.println("-------------------------------------------------");
        System.out.println("Bank client ");
        System.out.println("-------------------------------------------------");
        System.out.println("");
        System.out.println("Select an option : ");
        System.out.println("\t0. Quit");
        System.out.println("\t1. Create a new account.");
        System.out.println("\t2. Get an account information.");
        System.out.println("\t3. Make a transfer.");
        System.out.println("\t4. Credit an account.");
        System.out.println("\t5. Withdraw from an account.");
        System.out.println("\t6. Display this menu.");
        System.out.println("");
        System.out.print("Your choice : ");
        String choice = input();
        // Can only be null if there was a problem reading the input from the user.
        if (choice == null)
            return 0;

        // Return the int indicating the user's choice
        try
        {
            return Integer.parseInt(choice);
        }
        catch (NumberFormatException e)
        {
            // If the user input was not a number return as if the user asked to redisplay the menu.
            return 6;
        }
    }

    /**
     * This operation is used to get data from the keyboard.
     *
     * @return The input from the keyboard, or null if there is a problem reading the data.
     */
    private static String input()
    {
        try
        {
            java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(System.in));
            return reader.readLine();
        }
        catch (IOException ioe)
        {
            System.err.println("Problem reading from the keyboard, returning null");
            // Ignore this and return null
        }
        return null;
    }

    /**
     * This will display the menu in a loop asking for user input to create accounts or update them.
     */
    private void start()
    {
        while (true)
        {
            switch (menu())
            {
                case 0:
                    System.exit(0);
                case 1:
                    newAccount();
                    break;
                case 2:
                    getInfo();
                    break;
                case 3:
                    makeTransfer();
                    break;
                case 4:
                    makeCredit();
                    break;
                case 5:
                    makeWithdraw();
                    break;
                case 6:
                    break;
            }
        }
    }

    /**
     * This operation is used to create a new account.
     */
    private void newAccount()
    {
        System.out.println("");
        System.out.println("- Create a new account -");
        System.out.println("------------------------");
        System.out.println("");

        System.out.print("Name : ");
        String name = input();
        if (name == null)
        {
            System.out.println("Abort operation...");
            return;
        }

        System.out.print("Initial balance : ");
        String balance = input();
        if (balance == null)
        {
            System.out.println("Abort operation...");
            return;
        }

        float fbalance = 0;
        try
        {
            fbalance = new Float(balance).floatValue();
        }
        catch (java.lang.Exception ex)
        {
            System.out.println("Invalid float number, abort operation...");
            return;
        }

        try
        {
            Current current = OTSManager.get_current();
            System.out.println("Beginning a User transaction to create account");
            current.begin();
            Account acc = _bank.create_account(name);
            acc.credit(fbalance);
            System.out.println("Attempt to commit the account creation transaction");
            current.commit(false);
        }
        catch (Exception e)
        {
            System.err.println("ERROR - " + e);
        }
    }

    /**
     * This operation is used to get information about an account.
     */
    private void getInfo()
    {
        System.out.println("");
        System.out.println("- Get information about an account -");
        System.out.println("------------------------------------");
        System.out.println("");

        System.out.print("Name : ");

        String name = input();
        if (name == null)
        {
            System.out.println("Abort operation...");
            return;
        }

        try
        {
            Current current = OTSManager.get_current();
            System.out.println("Beginning a User transaction to get balance");
            current.begin();
            try
            {
                Account acc = _bank.get_account(name);
                System.out.println("Balance : " + acc.balance());
            }
            catch (NotExistingAccount nea)
            {
                System.out.println("Account not Found");
                current.rollback_only();
            }

            current.commit(false);
        }
        catch (Exception e)
        {
            System.err.println("ERROR - " + e);
        }
    }

    /**
     * This operation is used to make a transfer from an account to another account.
     */
    private void makeTransfer()
    {
        System.out.println("");
        System.out.println("- Make a transfer -");
        System.out.println("-------------------");
        System.out.println("");

        System.out.print("Take money from : ");

        String name_supplier = input();
        if (name_supplier == null)
        {
            System.out.println("Abort operation...");
            return;
        }

        System.out.print("Put money to : ");
        String name_consumer = input();
        if (name_consumer == null)
        {
            System.out.println("Abort operation...");
            return;
        }

        System.out.print("Transfer amount : ");
        String amount = input();
        if (amount == null)
        {
            System.out.println("Abort operation...");
            return;
        }

        float famount = 0;
        try
        {
            famount = new Float(amount).floatValue();
        }
        catch (java.lang.Exception ex)
        {
            System.out.println("Invalid float number, abort operation...");
            return;
        }

        Current current = null;
        try
        {
            current = OTSManager.get_current();
            System.out.println("Beginning a User transaction to Transfer money");
            current.begin();
            try
            {
                Account supplier = _bank.get_account(name_supplier);
                Account consumer = _bank.get_account(name_consumer);

                supplier.debit(famount);
                consumer.credit(famount);
            }
            catch (NotExistingAccount nea)
            {
                System.out.println("Account not Found");
                current.rollback_only();
            }

            current.commit(false);
        }
        catch (Exception e)
        {
            System.err.println("ERROR - " + e);
        }
    }

    /**
     * This operation is used to credit an account.
     */
    private void makeCredit()
    {
        System.out.println("");
        System.out.println("- Credit an Account -");
        System.out.println("-------------------");
        System.out.println("");

        System.out.print("Give the Account name : ");
        String name_consumer = input();
        if (name_consumer == null)
        {
            System.out.println("Abort operation...");
            return;
        }

        System.out.print("Amount to credit : ");
        String amount = input();
        if (amount == null)
        {
            System.out.println("Abort operation...");
            return;
        }

        float famount = 0;
        try
        {
            famount = new Float(amount).floatValue();
        }
        catch (java.lang.Exception ex)
        {
            System.out.println("Invalid float number, abort operation...");
            return;
        }

        try
        {
            Current current = OTSManager.get_current();
            System.out.println("Beginning a User transaction to  credit an account");
            current.begin();
            try
            {
                Account consumer = _bank.get_account(name_consumer);
                consumer.credit(famount);
            }
            catch (NotExistingAccount nea)
            {
                System.out.println("The requested account does not exist!");
                current.rollback_only();
            }

            current.commit(false);
        }
        catch (Exception e)
        {
            System.err.println("ERROR - " + e);
        }
    }

    /**
     * This operation is used to withdraw money from an account.
     */
    private void makeWithdraw()
    {
        System.out.println("");
        System.out.println("- Withrdaw from an Account -");
        System.out.println("-------------------");
        System.out.println("");

        System.out.print("Give the Account name : ");
        String name_debit = input();
        if (name_debit == null)
        {
            System.out.println("Abort operation...");
            return;
        }

        System.out.print("Amount to withdraw : ");
        String amount = input();
        if (amount == null)
        {
            System.out.println("Abort operation...");
            return;
        }

        float famount = 0;
        try
        {
            famount = new Float(amount).floatValue();
        }
        catch (java.lang.Exception ex)
        {
            System.out.println("Invalid float number, abort operation...");
            return;
        }

        try
        {
            Current current = OTSManager.get_current();
            System.out.println("Beginning a User transaction to withdraw from an account");
            current.begin();
            try
            {
                Account debiter = _bank.get_account(name_debit);
                debiter.debit(famount);
            }
            catch (NotExistingAccount nea)
            {
                System.out.println("The requested account does not exist!");
                current.rollback_only();
            }
            current.commit(false);
        }
        catch (Exception e)
        {
            System.err.println("ERROR - " + e);
        }
    }

    /**
     * This is the entry point into the JBoss Transaction product trailmap sample that uses a <CODE>Hashtable</CODE> to
     * store the bank accounts repository. It is part of the JTS trailmap when using local objects.
     *
     * @param args Not used in this example.
     */
    public static void main(String[] args)
    {
        // Define an ORB suitable for use by the JBoss Transactions product ORB portability layer.
        ORB myORB = null;
        // Define an object adapter suitable for use by the JBoss Transactions product ORB portability layer.
        RootOA myOA = null;
        try
        {
            // Initialize the ORB reference using the JBoss Transactions product ORB portability layer.
            myORB = ORB.getInstance("test");
            // Initialize the object adapter reference using the JBoss Transactions product ORB portability layer.
            myOA = OA.getRootOA(myORB);
            // Initialize the ORB using the JBoss Transactions product ORB portability layer.
            myORB.initORB(args, null);
            // Initialize the object adapter reference using the JBoss Transactions product ORB portability layer.
            myOA.initOA();
        }
        catch (Exception e)
        {
            // The ORB has not been correctly configured!
            // Display as much help as possible to the user track down the configuration problem
            System.err.println("Trailmap Error: ORB Initialisation failed: " + e);
            e.printStackTrace();
            System.exit(0);
        }

        // The volatile <CODE>Bank</CODE> implementation
        Bank bank = new Bank();

        // A client that can invoke operations on this <CODE>Bank</CODE>
        BankClient client = new BankClient(bank);

        // Start the client, this will run the CLI portion of the client code
        client.start();
    }
}