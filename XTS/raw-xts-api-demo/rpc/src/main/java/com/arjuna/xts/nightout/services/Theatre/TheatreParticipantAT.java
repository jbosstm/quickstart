package com.arjuna.xts.nightout.services.Theatre;

import com.arjuna.wst.*;

import java.io.Serializable;

/**
 * An adapter class that exposes the TheatreManager transaction lifecycle
 * API as a WS-T Atomic Transaction participant.
 * Also logs events to a TheatreView object.
 *
 * @author Jonathan Halliday (jonathan.halliday@arjuna.com)
 * @version $Revision: 1.3 $
 */
public class TheatreParticipantAT implements Durable2PCParticipant, Serializable
{
    /**
     * Participant instances are related to transaction instances
     * in a one to one manner.
     *
     * @param txID uniq id String for the transaction instance.
     */
    public TheatreParticipantAT(String txID)
    {
        // we need to save the txID for later use when calling
        // business logic methods in the theatreManger.
        this.txID = txID;
    }

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

        boolean success = getTheatreManager().prepareSeats(txID);

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
            getTheatreManager().cancelSeats(txID) ;
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

        boolean success = getTheatreManager().commitSeats(txID);

        // Log the outcome

        if (success)
        {
            getTheatreView().addMessage("Theatre tickets committed\n");
        }
        else
        {
            getTheatreView().addMessage("Something went wrong (Transaction not registered?)\n");
        }

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

        boolean success = getTheatreManager().cancelSeats(txID);

        // Log the outcome

        if (success)
        {
            getTheatreView().addMessage("Theatre booking cancelled\n");
        }
        else
        {
            getTheatreView().addMessage("Something went wrong (Transaction not registered?)\n");
        }

        getTheatreView().updateFields();
    }

    /**
     * Shortcut method which combines the prepare
     * and commit steps in a single operation.
     *
     * @throws WrongStateException
     * @throws SystemException
     */
    public void commitOnePhase() throws WrongStateException, SystemException
    {
        prepare();
        commit();
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
     * is passed to the backend business logic methods.
     */
    protected String txID;

    public TheatreView getTheatreView() {
        return TheatreView.getSingletonInstance();
    }

    public TheatreManager getTheatreManager() {
        return TheatreManager.getSingletonInstance();
    }
}