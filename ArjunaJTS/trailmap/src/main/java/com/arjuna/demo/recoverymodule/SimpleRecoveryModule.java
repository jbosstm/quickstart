package com.arjuna.demo.recoverymodule;

import com.arjuna.ats.arjuna.recovery.RecoveryModule;

import java.io.File;

/**
 * BEWARE: Implementing a <CODE>RecoveryModule</CODE> is a very advanced feature of the transaction service. It should
 * only be performed by users familiar with the all the concepts used in the JBoss Transactions product. Please see the
 * ArjunaCore guide for more information about <CODE>AbstractRecord</CODE>s.
 *
 * This implementation of the recovery module indicates how to write a recovery module to detect if an object is in an
 * inconsistent state. This implementation also shows how the recovery module implementation requires deep knowledge of
 * the specific type that it is responsible for recovering.
 */
public class SimpleRecoveryModule implements RecoveryModule
{
    /**
     * The name of the file which contains the current state of the current <CODE>SimpleRecord</CODE>.
     */
    private static String filename = SimpleRecord.filename;

    /**
     * Only used to display information to the user when the recovery manager is loaded.
     */
    public SimpleRecoveryModule()
    {
        System.out.println("The SimpleRecoveryModule is loaded");
    };

    /**
     * This method is called by the recovery subsystem to allow this module to check for uncommited
     * <CODE>SimpleRecord</CODE> states.
     *
     * This implementation is a simple system that only allows one outstanding <CODE>SimpleRecord</CODE> and so we check
     * the state of this record.
     */
    public void periodicWorkFirstPass()
    {
        // Indicate to the user what is happening
        System.out.println("The SimpleRecoveryModule - 1st pass ");

        // Check if there is a simple record to read
        File simpleRecord = new File(filename);
        // If a simple record exists
        if (simpleRecord.exists())
        {
            try
            {
                // Try to read the content of the SimpleRecord
                // We know with this implementation that it would be an error if an empty file exists as SimpleRecord
                // should not allow this
                java.io.FileInputStream file = new java.io.FileInputStream(simpleRecord);
                java.io.InputStreamReader input = new java.io.InputStreamReader(file);
                java.io.BufferedReader reader = new java.io.BufferedReader(input);

                // Try to read the content of the file
                String stringState = reader.readLine();
                if (stringState.compareTo("I'm prepared") == 0)
                    System.out.println("The transaction is in the prepared state");

                // NOTE: If this recovery module could handle more than one outstanding SimpleRecord it would now be
                // possible to remember that the record exists in a prepared state and during the second pass of
                // recovery check to see if it is still in the prepared state.

                // We have now completed first phase recovery
                file.close();
            }
            catch (java.io.IOException ioe)
            {
                System.err.println("Error: File exists but could not be read: " + ioe.getMessage());
            }
        }
        else
        {
            System.out.println("Nothing found on the Disk");
        }
    }

    public void periodicWorkSecondPass()
    {
        // Indicate to the user what is happening
        System.out.println("The SimpleRecoveryModule - 2nd pass");

        // Check if there is a simple record to read
        File simpleRecord = new File(filename);
        // If a simple record exists
        if (simpleRecord.exists())
        {
            try
            {
                // Try to read the content of the SimpleRecord
                // We know with this implementation that it would be an error if an empty file exists as SimpleRecord
                // should not allow this
                java.io.FileInputStream file = new java.io.FileInputStream(filename);
                java.io.InputStreamReader input = new java.io.InputStreamReader(file);
                java.io.BufferedReader reader = new java.io.BufferedReader(input);

                // Try to read the content of the file
                String stringState = reader.readLine();

                // Check the state of the record
                if (stringState.compareTo("I'm prepared") == 0)
                {
                    System.out.println("The record is still in the prepared state - Recovery is needed");

                    // NOTE: A full implementation of recovery manager could now try to recover the the
                    // <CODE>SimpleRecord</CODE>
                }
                // The recovery module is only really interested in finding prepared objects, this implementation
                // simply indicates that the state has been commited
                else if (stringState.compareTo("I'm Committed") == 0)
                {
                    System.out.println("The transaction has completed and committed");
                }

                // We have now completed second phase recovery
                file.close();
            }
            catch (java.io.IOException ioe)
            {
                System.err.println("Error: File exists but could not be read: " + ioe.getMessage());
            }
        }
        else
        {
            System.out.println("Nothing found on the Disk - Either there was no transaction or it as been rolled back");
        }
    }
}