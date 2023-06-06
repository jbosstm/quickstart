package com.jboss.jbosstm.xts.demo.services.restaurant;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.mw.wst11.TransactionManagerFactory;
import com.arjuna.mw.wst11.UserTransactionFactory;
import com.jboss.jbosstm.xts.demo.restaurant.IRestaurantServiceAT;

import jakarta.jws.HandlerChain;
import jakarta.jws.WebParam;
import jakarta.jws.WebService;
import jakarta.jws.WebMethod;
import jakarta.jws.soap.SOAPBinding;

/**
 * An adapter class that exposes the RestaurantManager business API as a
 * transactional Web Service. Also logs events to a RestaurantView object.
 *
 * @author Jonathan Halliday (jonathan.halliday@arjuna.com)
 * @version $Revision: 1.3 $
 */
@WebService(serviceName="RestaurantServiceATService", portName="RestaurantServiceAT",
        name = "IRestaurantServiceAT", targetNamespace = "http://www.jboss.com/jbosstm/xts/demo/Restaurant",
        wsdlLocation = "/WEB-INF/wsdl/RestaurantServiceAT.wsdl")
@HandlerChain(file = "/context-handlers.xml", name = "Context Handlers")
@SOAPBinding(style=SOAPBinding.Style.RPC)
public class RestaurantServiceAT implements IRestaurantServiceAT
{
    /**
     * Book a number of seats in the restaurant
     * Enrols a Participant if necessary, then passes
     * the call through to the business logic.
     *
     * @param how_many The number of seats to book
     */
    @WebMethod
    public void bookSeats(
            @WebParam(name = "how_many", partName = "how_many")
            int how_many)
    {
        RestaurantView restaurantView = RestaurantView.getSingletonInstance();
        RestaurantManager restaurantManager = RestaurantManager.getSingletonInstance();

        String transactionId = null;
        try
        {
            // get the transaction context of this thread:
            transactionId = UserTransactionFactory.userTransaction().toString();
            System.out.println("RestaurantServiceAT transaction id =" + transactionId);

            RestaurantParticipantAT restaurantParticipant = RestaurantParticipantAT.getParticipant(transactionId);

            if (restaurantParticipant != null)
            {
                // this service does not support repeated bookings in the same transaction
                // so mark the participant as invalid
                restaurantView.addMessage("id:" + transactionId + ". Participant already enrolled!");
                restaurantView.updateFields();
                System.err.println("bookSeats: request failed");
                // this ensures we do not try later to prepare the participant
                restaurantParticipant.invalidate();
                // throw away any local changes previously made on behalf of the participant
                restaurantManager.rollback(transactionId);
                return;
            }

            System.out.println("RestaurantServiceAT - enrolling...");
            // enlist the Participant for this service:
            restaurantParticipant = new RestaurantParticipantAT(transactionId);
            TransactionManagerFactory.transactionManager().enlistForDurableTwoPhase(restaurantParticipant, "org.jboss.jbossts.xts-demo:restaurantAT:" + new Uid().toString());
            RestaurantParticipantAT.recordParticipant(transactionId, restaurantParticipant);
        }
        catch (Exception e)
        {
            System.err.println("bookSeats: Participant enrolment failed");
            e.printStackTrace(System.err);
            return;
        }

        restaurantView.addMessage("******************************");

        restaurantView.addMessage("id:" + transactionId + ". Received a booking request for one table of " + how_many + " people");

        // invoke the backend business logic:
        restaurantManager.bookSeats(transactionId, how_many);

        restaurantView.addMessage("Request complete\n");
        restaurantView.updateFields();
    }
}