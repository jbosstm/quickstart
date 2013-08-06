package org.jboss.narayana.quickstarts.nonTransactionalResource.bookService;

import org.jboss.narayana.compensations.api.CompensationHandler;

import javax.inject.Inject;

/**
 * @author paul.robinson@redhat.com 02/08/2013
 */
public class RecallPackage implements CompensationHandler {

    @Inject
    OrderData orderData;

    @Override
    public void compensate() {
        //Recall the package somehow
        System.out.println("Recalling the package containing '" + orderData.getItem() + "' addressed to '" + orderData.getAddress() + "'");
    }
}

