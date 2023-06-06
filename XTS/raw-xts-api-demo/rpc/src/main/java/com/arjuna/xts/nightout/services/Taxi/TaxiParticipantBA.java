package com.arjuna.xts.nightout.services.Taxi;

import com.arjuna.wst.*;

import java.io.Serializable;

/**
 * An adapter class that exposes the TaxiManager transaction lifecycle
 * API as a WS-T Business Activity participant.
 * Also logs events to a TaxiView object.
 *
 * @author Jonathan Halliday (jonathan.halliday@arjuna.com)
 * @version $Revision: 1.2 $
 */
public class TaxiParticipantBA implements BusinessAgreementWithParticipantCompletionParticipant, Serializable
{
    /**
     * Participant instances are related to business method calls
     * in a one to one manner.
     *
     * @param txID uniq id String for the transaction instance.
     */
    public TaxiParticipantBA(String txID)
    {
        // we need to save the txID for later use when logging.
        this.txID = txID;
    }

    /**
     * The transaction has completed successfully. The participant previously
     * informed the coordinator that it was ready to complete.
     *
     * @throws WrongStateException never in this implementation.
     * @throws SystemException never in this implementation.
     */

    public void close() throws WrongStateException, SystemException
    {
        // let the manager know that this activity no longer requires the option of compensation

        System.out.println("TaxiParticipantBA.close");

        if (!getTaxiManager().closeTaxi(txID)) {
            // throw a WrongStateException to indicate that we were not expecting a close
            System.out.println("TaxiParticipantBA.close : not expecting a close for BA participant " + txID);

            throw new WrongStateException("Unexpected close for BA participant " + txID);
        }

        getTaxiView().addMessage("id:" + txID + ". Close called on participant: " + this.getClass());
        getTaxiView().updateFields();
    }

    /**
     * The transaction has cancelled, and the participant should undo any work.
     * The participant cannot have informed the coordinator that it has
     * completed.
     *
     * @throws WrongStateException never in this implementation.
     * @throws SystemException never in this implementation.
     */

    public void cancel() throws WrongStateException, SystemException
    {
        // let the manager know that this activity is being cancelled

        System.out.println("TaxiParticipantBA.cancel");

        if (!getTaxiManager().cancelTaxi(txID)) {
            // throw a WrongStateException to indicate that we were not expecting a close
            System.out.println("TaxiParticipantBA.cancel : not expecting a cancel for BA participant " + txID);

            throw new WrongStateException("Unexpected cancel for BA participant " + txID);
        }

        getTaxiView().addMessage("id:" + txID + ". Cancel called on participant: " + this.getClass().toString());
        getTaxiView().updateFields();
    }

    /**
     * The transaction has cancelled. The participant previously
     * informed the coordinator that it had finished work but could compensate
     * later if required, so it is now requested to do so.
     *
     * @throws WrongStateException never in this implementation.
     * @throws SystemException always, because this implementation does not support compensation.
     */

    public void compensate() throws FaultedException, WrongStateException, SystemException
    {
        System.out.println("TaxiParticipantBA.compensate");

        getTaxiView().addPrepareMessage("id:" + txID + ". Attempting to compensate participant: " + this.getClass().toString());

        getTaxiView().updateFields();

        // tell the manager to compensate

        try {
            if (!getTaxiManager().compensateTaxi(txID)) {
                // throw a WrongStateException to indicate that we were not expecting a close
                System.out.println("RestaurantParticipantBA.compensate : not expecting a compensate for BA participant " + txID);

                getTaxiView().addMessage("id:" + txID + ". Failed to compensate participant: " + this.getClass().toString());
                getTaxiView().updateFields();

                throw new WrongStateException("Unexpected compensate for BA participant " + txID);
            }
        } catch (FaultedException fe) {
            getTaxiView().addMessage("id:" + txID + ". FaultedException when compensating participant: " + this.getClass().toString());

            getTaxiView().updateFields();
            throw fe;
        }

        getTaxiView().addMessage("id:" + txID + ". Compensated participant: " + this.getClass().toString());
        getTaxiView().updateFields();
    }

    public String status () throws SystemException
    {
        return null;
    }

    public void unknown() throws SystemException
    {
        // used for calbacks during crash recovery. This impl is not recoverable
    }

    public void error() throws SystemException
    {
        // used for calbacks during crash recovery. This impl is not recoverable
    }

    /**
     * Id for the transaction which this participant instance relates to.
     * Set by the service (via contrtuctor) at enrolment time, this value
     * is used in informational log messages.
     */
    protected String txID;

    public TaxiView getTaxiView() {
        return TaxiView.getSingletonInstance();
    }

    public TaxiManager getTaxiManager() {
        return TaxiManager.getSingletonInstance();
    }
}