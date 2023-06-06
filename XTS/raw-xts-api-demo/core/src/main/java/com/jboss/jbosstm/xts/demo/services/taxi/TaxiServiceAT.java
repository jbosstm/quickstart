package com.jboss.jbosstm.xts.demo.services.taxi;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.mw.wst11.TransactionManagerFactory;
import com.arjuna.mw.wst11.UserTransactionFactory;
import com.jboss.jbosstm.xts.demo.taxi.ITaxiServiceAT;

import jakarta.jws.WebService;
import jakarta.jws.HandlerChain;
import jakarta.jws.WebMethod;
import jakarta.jws.soap.SOAPBinding;

/**
 * An adapter class that exposes the TaxiManager business API as a
 * transactional Web Service. Also logs events to a TaxiView object.
 *
 * @author Jonathan Halliday (jonathan.halliday@arjuna.com)
 * @version $Revision: 1.3 $
 */
@WebService(serviceName="TaxiServiceATService", portName="TaxiServiceAT",
        name = "ITaxiServiceAT", targetNamespace = "http://www.jboss.com/jbosstm/xts/demo/Taxi",
        wsdlLocation = "/WEB-INF/wsdl/TaxiServiceAT.wsdl")
@HandlerChain(file = "/context-handlers.xml", name = "Context Handlers")
@SOAPBinding(style=SOAPBinding.Style.RPC)
public class TaxiServiceAT implements ITaxiServiceAT
{
    /**
     * Book a taxi
     * Enrols a Participant if necessary, then passes
     * the call through to the business logic.
     */
    @WebMethod
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
                TransactionManagerFactory.transactionManager().enlistForDurableTwoPhase(taxiParticipant, "org.jboss.jbossts.xts-demo:taxiAT:" + new Uid().toString());
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

        // invoke the backend business logic:
        TaxiManager.getSingletonInstance().bookTaxi(transactionId);

        taxiView.addMessage("Request complete\n");
        taxiView.updateFields();
    }
}