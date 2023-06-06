package com.arjuna.demo.jts.explicitremotebank;

import com.arjuna.orbportability.OA;
import com.arjuna.orbportability.ORB;
import org.omg.CosTransactions.Control;
import org.omg.CosTransactions.Inactive;
import org.omg.CosTransactions.RecoveryCoordinator;
import org.omg.CosTransactions.Resource;
import org.omg.CosTransactions.Unavailable;

/**
 * The account object reflects a non-persistent transaction-aware representation of a bank account. As a bank account,
 * it has a balance associated with it. As a transaction-aware entity it has a trailmap defined ResourcePOA associated
 * with it (a single ResourcePOA is created for each transaction that uses the account).
 *
 * This object allows an account balance to be interrogated, increased or decreased, all under transactional control.
 *
 * For this trailmap example, the account must be interacted with by explicitly passing a transaction contorl for the
 * resource to be registered with.
 */
public class AccountImpl extends AccountPOA
{
    /**
     * The current value of the bank account.
     */
    float _balance;

    /**
     * The name of the bank account holder.
     */
    private String _name;

    /**
     * The transactional update entity associated with this account.
     */
    AccountResource accRes = null;

    /**
     * A CORBA reference to the account resource suitable for registration with the transaction coordinator.
     */
    private Resource ref;

    /**
     * Create a new zerioed bank account and associate a name with it.
     *
     * @param name  The name of the bank account holder.
     */
    public AccountImpl(String name)
    {
        _name = name;
        _balance = 0;
    }

    /**
     * Get the current balance of the account within the scope of the current transaction.
     *
     * @param ctrl  The transaction control to indicate the transaction to acquire the balance under.
     *
     * @return  The balance as it stands for this transaction.
     */
    public float balance(Control ctrl)
    {
        return getResource(ctrl).balance();
    }

    /**
     * Credit this account under control of the current transaction.
     *
     * @param ctrl  The transaction control to indicate the transaction to update the account under.
     * @param value The amount to increase the account balance by.
     */
    public void credit(Control ctrl, float value)
    {
        getResource(ctrl).credit(value);
    }

    /**
     * Debit this account under control of the current transaction.
     *
     * @param ctrl  The transaction control to indicate the transaction to update the account under.
     * @param value The amount to decrease the account balance by.
     */
    public void debit(Control ctrl, float value)
    {
        getResource(ctrl).debit(value);
    }

    /**
     * Get and enlist the account resource ready for access.
     *
     * @param control   The transaction to enlist the resource within.
     *
     * @return          A ResourcePOA which has been enlisted in the current transaction.
     */
    private AccountResource getResource(Control control)
    {
        if (accRes == null)
        {
            accRes = new AccountResource(this, _name);
            ref = org.omg.CosTransactions.ResourceHelper.narrow(OA.getRootOA(ORB.getInstance("ServerSide")).corbaReference(accRes));

            try
            {
                RecoveryCoordinator recoverycoordinator = control.get_coordinator().register_resource(ref);
            }
            catch (Inactive inactive)
            {
                // Display as much help as possible to the user track down the configuration problem
                System.err.println("CosTransactions Error: Could not register_resource() Inactive exception: " + inactive);
                inactive.printStackTrace();
                System.exit(0);
            }
            catch (Unavailable unavailable)
            {
                // Display as much help as possible to the user track down the configuration problem
                System.err.println("CosTransactions Error: Could not get_coordinator() Unavailable exception: " + unavailable);
                unavailable.printStackTrace();
                System.exit(0);
            }
            catch (org.omg.CORBA.SystemException systemException)
            {
                // Display as much help as possible to the user track down the configuration problem
                System.err.println("CosTransactions Error: Could not get_current() SystemException: " + systemException);
                systemException.printStackTrace();
                System.exit(0);
            }
        }
        return accRes;
    }
}