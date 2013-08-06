package org.jboss.narayana.quickstarts.nonTransactionalResource.bookService;

import org.jboss.narayana.compensations.api.Compensatable;

import javax.inject.Inject;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author paul.robinson@redhat.com 02/08/2013
 */
public class BookService {

    @Inject
    PackageDispatcher packageDispatcher;

    @Inject
    InvoicePrinter invoicePrinter;

    private static AtomicInteger orderId = new AtomicInteger(0);

    @Compensatable
    public void buyBook(String item, String address) {

        packageDispatcher.dispatch(item, address);
        invoicePrinter.print(orderId.getAndIncrement(), "Invoice body would go here, blah blah blah");

        //other activities, such as updating inventory and charging the customer
    }

}
