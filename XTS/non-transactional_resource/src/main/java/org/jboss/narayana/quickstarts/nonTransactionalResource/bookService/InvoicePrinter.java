package org.jboss.narayana.quickstarts.nonTransactionalResource.bookService;

import org.jboss.narayana.compensations.api.TxCompensate;

import javax.inject.Inject;

/**
 * @author paul.robinson@redhat.com 02/08/2013
 */
public class InvoicePrinter {

    @Inject
    InvoiceData invoiceData;

    public static boolean hasInk = true;

    @TxCompensate(DestroyInvoice.class)
    public void print(Integer invoiceId, String invoiceBody) {

        if (!hasInk) {
            throw new RuntimeException("Printer has run out of ink. Unable to print invoice");
        }

        invoiceData.setInvoiceBody(invoiceBody);
        invoiceData.setInvoiceId(invoiceId);

        System.out.println("Printing the invoice ready for posting...");
    }

}
