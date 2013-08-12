package org.jboss.narayana.quickstarts.wsba.simple;

import org.jboss.narayana.compensations.api.CompensationHandler;
import org.jboss.narayana.txframework.api.management.TXDataMap;

import javax.inject.Inject;

/**
 * @author paul.robinson@redhat.com 26/05/2013
 */
public class CancelOrder implements CompensationHandler {

    /*
        The @TXDataMap injection provides a map that that is isolated to both the transaction and this participant. This allows the service to store data that
        can be retrieved when the protocol lifecycle methods are invoked by the coordinator (those annotated with @Compensate, @Cancel, etc).
        The Map is isolated within a particular transaction; therefore it is safe for multiple transactions to use this map without seeing each others' data.
        The data is automatically removed after the transaction has ended.
     */
    @Inject
    private OrderData orderData;

    /**
     * The BA has cancelled. The participant previously informed the coordinator that it had finished work but could
     * compensate later if required, and it is now requested to do so.
     */
    public void compensate() {

        System.out.println("[SERVICE] @Compensate called");

        //Lookup the item Do something to cancel the order, maybe remove it from the database. This is outside the scope of this quickstart.

        /*
         * Email the customer to notify them that the order ws cancelled.
         */
        EmailSender.sendEmail(orderData.getEmailAddress(), EmailSender.MAIL_TEMPLATE_CANCELLATION);
    }

}
