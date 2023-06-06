package com.arjuna.xts.nightout.services.Taxi;

import com.arjuna.mw.wst.UserTransactionFactory;
import com.arjuna.mw.wst.TransactionManagerFactory;
import com.arjuna.ats.arjuna.common.Uid;

/**
 * An adapter class that exposes the TaxiManager business API as a
 * transactional Web Service. Also logs events to a TaxiView object.
 *
 * @author Jonathan Halliday (jonathan.halliday@arjuna.com)
 * @version $Revision: 1.3 $
 */
public class TaxiServiceAT implements ITaxiService
{
    /**
     * Book a taxi
     * Enrols a Participant if necessary, then passes
     * the call through to the business logic.
     */
    public void bookTaxi()
    {
        TaxiView taxiView = TaxiView.getSingletonInstance();
        TaxiManager taxiManager = TaxiManager.getSingletonInstance();

        String transactionId = null;
        try
        {
            // get the transaction context of this thread:
            transactionId = UserTransactionFactory.userTransaction().toString();
            System.out.println("TaxiServiceAT transaction id =" + transactionId);

            if (!taxiManager.knowsAbout(transactionId))
            {
                System.out.println("TaxiServiceAT - enrolling...");
                // enlist the Participant for this service:
                TaxiParticipantAT taxiParticipant = new TaxiParticipantAT(transactionId);
                TransactionManagerFactory.transactionManager().enlistForDurableTwoPhase(taxiParticipant, "org.jboss.jbossts.xts-demorpc:taxiAT:" + new Uid().toString());
            }
        }
        catch (Exception e)
        {
            System.err.println("bookTaxi: Participant enrolment failed");
            e.printStackTrace(System.err);
            return;
        }

        taxiView.addMessage("******************************");

        taxiView.addMessage("id:" + transactionId.toString() + ". Received a taxi booking request");

        TaxiManager.getSingletonInstance().bookTaxi(transactionId);

        taxiView.addMessage("Request complete\n");
        taxiView.updateFields();
    }
}