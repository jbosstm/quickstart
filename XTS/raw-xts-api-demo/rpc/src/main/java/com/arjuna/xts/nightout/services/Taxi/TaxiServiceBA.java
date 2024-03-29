package com.arjuna.xts.nightout.services.Taxi;

import com.arjuna.wst.*;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.mw.wst.BusinessActivityManager;
import com.arjuna.mw.wst.BusinessActivityManagerFactory;

/**
 * An adapter class that exposes the TaxiManager business API as a
 * transactional Web Service. Also logs events to a TaxiView object.
 *
 * @author Jonathan Halliday (jonathan.halliday@arjuna.com)
 * @version $Revision: 1.5 $
 */
public class TaxiServiceBA implements ITaxiServiceBA
{
    /**
     * Book a taxi
     * Enrols a Participant if necessary and passes
     * the call through to the business logic.
     *
     * @return true on success, false otherwise.
     */
    public boolean bookTaxi()
    {
        TaxiView taxiView = TaxiView.getSingletonInstance();
        TaxiManager taxiManager = TaxiManager.getSingletonInstance();

        BusinessActivityManager activityManager = BusinessActivityManagerFactory.businessActivityManager();

        // get the transaction context of this thread:
        String transactionId = null;
        try
        {
            transactionId = activityManager.currentTransaction().toString();
        }
        catch (SystemException e)
        {
            System.err.println("bookTaxi: unable to obtain a transaction context!");
            e.printStackTrace(System.err);
            return false;
        }

        // log the event:
        System.out.println("TaxiServiceBA transaction id =" + transactionId);

        taxiView.addMessage("******************************");

        taxiView.addPrepareMessage("id:" + transactionId.toString() + ". Received a taxi booking request");
        taxiView.updateFields();

        // invoke the backend business logic:
        taxiManager.bookTaxi(transactionId);

        // attempt to finalise the booking
        if (taxiManager.prepareTaxi(transactionId))
        {
            taxiView.addMessage("id:" + transactionId + ". Seats prepared, trying to commit and enlist compensation Participant");
            taxiView.updateFields();

            // it worked, so now we need a participant enlisted in case of compensation:
            TaxiParticipantBA taxiParticipant = new TaxiParticipantBA(transactionId);
            // enlist the Participant for this service:
            BAParticipantManager participantManager = null;
            try
            {
                participantManager = activityManager.enlistForBusinessAgreementWithParticipantCompletion(taxiParticipant, "com.arjuna.xts-demorpc:taxiBA:" + new Uid().toString());
            }
            catch (Exception e)
            {
                taxiView.addMessage("id:" + transactionId + ". Participant enrolement failed");
                taxiManager.cancelTaxi(transactionId);
                System.err.println("bookTaxi: Participant enrolment failed");
                e.printStackTrace(System.err);
                return false;
            }

            // finish the booking in the backend ensuring it is compensatable:
            taxiManager.commitTaxi(transactionId, true);

            try
            {
                // tell the manager we have finished our work:
                participantManager.completed();
            }
            catch (Exception e)
            {
                System.err.println("bookTaxi: 'completed' callback failed");
                taxiManager.cancelTaxi(transactionId);
                e.printStackTrace(System.err);
                return false;
            }
        }
        else
        {
            taxiView.addMessage("id:" + transactionId + ". Failed to reserve taxi. Cancelling.");
            taxiManager.cancelTaxi(transactionId);
            taxiView.updateFields();
            return false;
        }

        taxiView.addMessage("Request complete\n");
        taxiView.updateFields();

        return true;
    }
}