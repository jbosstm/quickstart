package com.jboss.jbosstm.xts.demo.services.theatre;

import com.arjuna.wst.*;

import java.io.Serializable;
import java.util.HashMap;

/**
 * An adapter class that exposes the TheatreManager as a WS-T Atomic Transaction participant.
 * Also logs events to a TheatreView object.
 *
 * @author Jonathan Halliday (jonathan.halliday@arjuna.com)
 * @version $Revision: 1.3 $
 */
public class TheatreParticipantAT implements Durable2PCParticipant, Serializable
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
    public TheatreParticipantAT(String txID, int[] bookings)
    {
        // we need to save the txID for later use when calling
        // business logic methods in the theatreManger.
        this.txID = txID;
        // we need to remember which seating areas have already been booked
        this.bookings = bookings;
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
     * @return trus on success, false otherwise.
     * @throws WrongStateException
     * @throws SystemException
     */
    public Vote prepare() throws WrongStateException, SystemException
    {
        // Log the event and invoke the prepare operation
        // on the backend business logic.

        System.out.println("TheatreParticipantAT.prepare");

        getTheatreView().addPrepareMessage("id:" + txID + ". Prepare called on participant: " + this.getClass().toString());

        boolean success = getTheatreManager().prepare(txID);

        // Log the outcome and map the return value from
        // the business logic to the appropriate Vote type.


        if (success)
        {
            getTheatreView().addMessage("Theatre prepared successfully. Returning 'Prepared'\n");
            getTheatreView().updateFields();
            return new Prepared();
        }
        else
        {
            getTheatreView().addMessage("Prepare failed (not enough seats?) Returning 'Aborted'\n");
            getTheatreView().updateFields();
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

        System.out.println("TheatreParticipantAT.commit");

        getTheatreView().addMessage("id:" + txID + ". Commit called on participant: " + this.getClass().toString());

        getTheatreManager().commit(txID);

        getTheatreView().addMessage("Theatre tickets committed\n");

        getTheatreView().updateFields();
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

        System.out.println("TheatreParticipantAT.rollback");

        getTheatreView().addMessage("id:" + txID + ". Rollback called on participant: " + this.getClass().toString());

        getTheatreManager().rollback(txID);

        getTheatreView().addMessage("Theatre booking cancelled\n");

        getTheatreView().updateFields();
    }

    public void unknown() throws SystemException
    {
        // used for calbacks during crash recovery. This impl is not recoverable
    }

    public void error() throws SystemException
    {
        // used for calbacks during crash recovery. This impl is not recoverable
    }

    /************************************************************************/
    /* tracking active participants                                         */
    /************************************************************************/
    /**
     * keep track of a participant
     * @param txID the participant's transaction identifier
     * @param participant the participant to be recorded
     */
    public static synchronized void recordParticipant(String txID, TheatreParticipantAT participant)
    {
        participants.put(txID, participant);
    }

    /**
     * forget about a participant
     * @param txID the participant's transaction identifier
     * @return the removed participant
     */
    public static synchronized TheatreParticipantAT removeParticipant(String txID)
    {
        return participants.remove(txID);
    }

    /**
     * lookup a participant
     * @param txID the participant's transaction identifier
     * @return the participant
     */
    public static synchronized TheatreParticipantAT getParticipant(String txID)
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
     * array containing bookings for each of the seating areas. each area is booked in its own
     * service request but we need this info in order to be able to detect repeated bookings.
     */
    protected int[] bookings;
    
    /**
     * this is true by default but we invalidate the participant if the client makes invalid requests
     */
    protected boolean valid;
    
    private TheatreView getTheatreView() {
        return TheatreView.getSingletonInstance();
    }

    private TheatreManager getTheatreManager() {
        return TheatreManager.getSingletonInstance();
    }

    /**
     * table of currently active participants
     */
    private static HashMap<String, TheatreParticipantAT> participants = new HashMap<String,  TheatreParticipantAT>();
}