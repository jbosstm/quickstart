package org.jboss.narayana.quickstarts.nonTransactionalResource.bookService;

import org.jboss.narayana.compensations.api.TxCompensate;

import javax.inject.Inject;

/**
 * @author paul.robinson@redhat.com 02/08/2013
 */
public class PackageDispatcher {

    @Inject
    OrderData orderData;

    @TxCompensate(RecallPackage.class)
    public void dispatch(String item, String address) {

        orderData.setAddress(address);
        orderData.setItem(item);
        //Dispatch the package
        System.out.println("Dispatching the package...");
    }

}
