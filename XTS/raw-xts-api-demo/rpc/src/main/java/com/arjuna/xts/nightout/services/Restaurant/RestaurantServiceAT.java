package com.arjuna.xts.nightout.services.Restaurant;

import com.arjuna.mw.wst.UserTransactionFactory;
import com.arjuna.mw.wst.TransactionManagerFactory;
import com.arjuna.ats.arjuna.common.Uid;

/**
 * An adapter class that exposes the RestaurantManager business API as a
 * transactional Web Service. Also logs events to a RestaurantView object.
 *
 * @author Jonathan Halliday (jonathan.halliday@arjuna.com)
 * @version $Revision: 1.3 $
 */
public class RestaurantServiceAT implements IRestaurantService
{
    /**
     * Book a number of seats in the restaurant
     * Enrols a Participant if necessary, then passes
     * the call through to the business logic.
     *
     * @param how_many The number of seats to book
     */
    public void bookSeats(int how_many)
    {
        RestaurantView restaurantView = RestaurantView.getSingletonInstance();
        RestaurantManager restaurantManager = RestaurantManager.getSingletonInstance();

        String transactionId = null;
        try
        {
            // get the transaction context of this thread:
            transactionId = UserTransactionFactory.userTransaction().toString();
            System.out.println("RestaurantServiceAT transaction id =" + transactionId);

            if (!restaurantManager.knowsAbout(transactionId))
            {
                System.out.println("RestaurantServiceAT - enrolling...");
                // enlist the Participant for this service:
                RestaurantParticipantAT restaurantParticipant = new RestaurantParticipantAT(transactionId);
                TransactionManagerFactory.transactionManager().enlistForDurableTwoPhase(restaurantParticipant, "org.jboss.jbossts.xts-demorpc:restaurantAT:" + new Uid().toString());
            }
        }
        catch (Exception e)
        {
            System.err.println("bookSeats: Participant enrolment failed");
            e.printStackTrace(System.err);
            return;
        }

        restaurantView.addMessage("******************************");

        restaurantView.addMessage("id:" + transactionId + ". Received a booking request for one table of " + how_many + " people");

        restaurantManager.bookSeats(transactionId, how_many);

        restaurantView.addMessage("Request complete\n");
        restaurantView.updateFields();
    }
}