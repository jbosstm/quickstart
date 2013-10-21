package org.jboss.narayana.quickstarts.nonTransactionalResource.bookService;

import org.jboss.narayana.compensations.api.CompensationHandler;

import javax.inject.Inject;

/**
 * @author paul.robinson@redhat.com 02/08/2013
 */
public class DestroyInvoice implements CompensationHandler {

    @Inject
    InvoiceData invoiceData;

    @Override
    public void compensate() {
        //Recall the package somehow
        System.out.println("Hunt down invoice with id '" + invoiceData.getInvoiceId() + "' and destroy it...");
    }
}

