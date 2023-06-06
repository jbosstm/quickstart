package com.jboss.jbosstm.xts.demo.services.restaurant;

import com.arjuna.wst.*;

import java.io.Serializable;
import java.util.HashMap;

/**
 * An adapter class that exposes the RestaurantManager as a WS-T Atomic Transaction participant.
 * Also logs events to a RestaurantView object.
 *
 * @author Jonathan Halliday (jonathan.halliday@arjuna.com)
 * @version $Revision: 1.3 $
 */
public class RestaurantParticipantAT implements Durable2PCParticipant, Serializable
{
    /************************************************************************/
    /* public methods                                                       */
    /************************************************************************/
    /**
     * Participant instances are related to transaction instances
     * in a one to one manner.
     *
     * @param txID uniq id String for the transaction instance.
     */
    public RestaurantParticipantAT(String txID)
    {
        // we need to save the txID for later use when calling
        // business logic methods in the restaurantManger.
        this.txID = txID;
        // we may invalidate the participant later if something goes wrong
        this.valid = true;
    }

    /**
     * accessor for participant transaction id
     * @return the participant transaction id
     */
    public String getTxID() {
        return txID;
    }

    /************************************************************************/
    /* Durable2PCParticipant methods                                        */
    /************************************************************************/
    /**
     * Invokes the prepare step of the business logic,
     * reporting activity and outcome.
     *
     * @return Prepared where possible, Aborted where necessary.
     * @throws WrongStateException
     * @throws SystemException
     */
    public Vote prepare() throws WrongStateException, SystemException
    {
        // Log the event and invoke the prepare operation
        // on the backend business logic.

        System.out.println("RestaurantParticipantAT.prepare");

        getRestaurantView().addPrepareMessage("id:" + txID + ". Prepare called on participant: " + this.getClass().toString());

        boolean success = getRestaurantManager().prepare(txID);

        // Log the outcome and map the return value from
        // the business logic to the appropriate Vote type.

        if (success)
        {
            getRestaurantView().addMessage("Seats prepared successfully. Returning 'Prepared'\n");
            getRestaurantView().updateFields();
            return new Prepared();
        }
        else
        {
            getRestaurantView().addMessage("Prepare failed (not enough seats?) Returning 'Aborted'\n");
            getRestaurantView().updateFields();
            // forget about the participant
            removeParticipant(txID);
            return new Aborted();
        }
    }

    /**
     * Invokes the commit step of the business logic,
     * reporting activity and outcome.
     *
     * @throws WrongStateException
     * @throws SystemException
     */
    public void commit() throws WrongStateException, SystemException
    {
        // Log the event and invoke the commit operation
        // on the backend business logic.

        System.out.println("RestaurantParticipantAT.commit");

        getRestaurantView().addMessage("id:" + txID + ". Commit called on participant: " + this.getClass().toString());

        getRestaurantManager().commit(txID);

        // Log the outcome

        getRestaurantView().addMessage("Seats committed\n");

        getRestaurantView().updateFields();

        // forget about the participant
        removeParticipant(txID);
    }

    /**
     * Invokes the rollback operation on the business logic,
     * reporting activity and outcome.
     *
     * @throws WrongStateException
     * @throws SystemException
     */
    public void rollback() throws WrongStateException, SystemException
    {
        // Log the event and invoke the rollback operation
        // on the backend business logic.

        System.out.println("RestaurantParticipantAT.rollback");

        getRestaurantView().addMessage("id:" + txID + ". Rollback called on participant: " + this.getClass().toString());

        getRestaurantManager().rollback(txID);

        getRestaurantView().addMessage("Seats booking cancelled\n");
        
        getRestaurantView().updateFields();
        // forget about the participant
        removeParticipant(txID);
    }

    public void unknown() throws SystemException
    {
        // forget about the participant
        participants.put(txID, null);
    }

    public void error() throws SystemException
    {
        // forget about the participant
        participants.put(txID, null);
    }

    /************************************************************************/
    /* tracking active participants                                         */
    /************************************************************************/
    /**
     * keep track of a participant
     * @param txID the participant's transaction id
     * @param participant the participant
     */
    public static synchronized void recordParticipant(String txID, RestaurantParticipantAT participant)
    {
        participants.put(txID, participant);
    }

    /**
     * forget about a participant
     * @param txID the participant's transaction id
     */
    public static synchronized RestaurantParticipantAT removeParticipant(String txID)
    {
        return participants.remove(txID);
    }

    /**
     * lookup a participant
     * @param txID the participant's transaction id
     * @param participant the participant
     */
    public static synchronized RestaurantParticipantAT getParticipant(String txID)
    {
        return participants.get(txID);
    }

    /**
     * mark a participant as invalid
     */
    public void invalidate()
    {
        valid = false;
    }

    /**
     * check if a participant is invalid
     *
     * @return true if the participant is still valid otherwise false
     */
    public boolean isValid()
    {
        return valid;
    }

    /************************************************************************/
    /* private implementation                                               */
    /************************************************************************/
    /**
     * Id for the transaction which this participant instance relates to.
     * Set by the service (via contrtuctor) at enrolment time, this value
     * is passed to the backend business logic methods.
     */
    protected String txID;
    /**
     * this is true by default but we invalidate the participant if the client makes invalid requests
     */
    protected boolean valid;

    private RestaurantView getRestaurantView() {
        return RestaurantView.getSingletonInstance();
    }

    private RestaurantManager getRestaurantManager() {
        return RestaurantManager.getSingletonInstance();
    }

    /**
     * table of currently active participants
     */
    private static HashMap<String, RestaurantParticipantAT> participants = new HashMap<String,  RestaurantParticipantAT>();

}