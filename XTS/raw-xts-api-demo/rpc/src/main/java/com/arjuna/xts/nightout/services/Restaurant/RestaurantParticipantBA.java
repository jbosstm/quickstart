package com.arjuna.xts.nightout.services.Restaurant;

import com.arjuna.wst.*;

import java.io.Serializable;

/**
 * An adapter class that exposes the RestaurantManager transaction lifecycle
 * API as a WS-T Business Activity participant.
 * Also logs events to a RestaurantView object.
 *
 * @author Jonathan Halliday (jonathan.halliday@arjuna.com)
 * @version $Revision: 1.3 $
 */
public class RestaurantParticipantBA implements BusinessAgreementWithParticipantCompletionParticipant, Serializable
{
    /**
     * Participant instances are related to business method calls
     * in a one to one manner.
     *
     * @param txID     uniq id String for the transaction instance.
     * @param how_many seats to book/compensate.
     */
    public RestaurantParticipantBA(String txID, int how_many)
    {
        // we need to save the txID for later use when logging.
        this.txID = txID;
        // we also need the business paramater(s) in case of compensation
        this.seatCount = how_many;
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

        System.out.println("RestaurantParticipantBA.close");

        if (!getRestaurantManager().closeSeats(txID)) {
            // throw a WrongStateException to indicate that we were not expecting a close
            System.out.println("RestaurantParticipantBA.close : not expecting a close for BA participant " + txID);

            throw new WrongStateException("Unexpected close for BA participant " + txID);
        }

        getRestaurantView().addMessage("id:" + txID + ". Close called on participant: " + this.getClass());

        getRestaurantView().updateFields();
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
        // let the manager know that this activity has been cancelled

        System.out.println("RestaurantParticipantBA.cancel");

        if (!getRestaurantManager().cancelSeats(txID)) {
            // throw a WrongStateException to indicate that we were not expecting a close
            System.out.println("RestaurantParticipantBA.cancel : not expecting a cancel for BA participant " + txID);

            throw new WrongStateException("Unexpected cancel for BA participant " + txID);
        }

        getRestaurantView().addMessage("id:" + txID + ". Cancel called on participant: " + this.getClass().toString());
        getRestaurantView().updateFields();
    }

    /**
     * The transaction has cancelled. The participant previously
     * informed the coordinator that it had finished work but could compensate
     * later if required, so it is now requested to do so.
     *
     * @throws WrongStateException never in this implementation.
     * @throws SystemException if unable to perform the compensating transaction.
     */

    public void compensate() throws FaultedException, WrongStateException, SystemException
    {
        System.out.println("RestaurantParticipantBA.compensate");

        // Log the event and perform a compensating transaction
        // on the backend business logic.

        getRestaurantView().addPrepareMessage("id:" + txID + ". Compensate called on participant: " + this.getClass().toString());

        getRestaurantView().updateFields();

        // tell the manager to compensate

        try {
            if (!getRestaurantManager().compensateSeats(txID)) {
                // throw a WrongStateException to indicate that we were not expecting a close
                System.out.println("RestaurantParticipantBA.compensate : not expecting a compensate for BA participant " + txID);

                throw new WrongStateException("Unexpected compensate for BA participant " + txID);
            }
        } catch (FaultedException fe) {
            getRestaurantView().addMessage("id:" + txID + ". FaultedException when compensating participant: " + this.getClass().toString());

            getRestaurantView().updateFields();
            throw fe;
        }

        getRestaurantView().addMessage("id:" + txID + ". Compensated participant: " + this.getClass().toString());

        getRestaurantView().updateFields();
    }

    public String status()
    {
        return null ;
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

    /**
     * Copy of business state information, may be needed during compensation.
     */
    protected int seatCount;

    public RestaurantView getRestaurantView() {
        return RestaurantView.getSingletonInstance();
    }

    public RestaurantManager getRestaurantManager() {
        return RestaurantManager.getSingletonInstance();
    }
}