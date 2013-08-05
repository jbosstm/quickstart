/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.narayana.quickstarts.nonTransactionalResource.bookService;

import org.jboss.narayana.compensations.api.Compensatable;
import org.jboss.narayana.compensations.api.CompensationTransactionType;

import javax.inject.Inject;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author gytis@redhat.com 05/08/2013
 * @author paul.robinson@redhat.com 02/08/2013
 */
public class BookService {

    @Inject
    PackageDispatcher packageDispatcher;

    @Inject
    InvoicePrinter invoicePrinter;

    private static AtomicInteger orderId = new AtomicInteger(0);

    @Compensatable(cancelOn = PackageDispatcherException.class, dontCancelOn = InvoicePrinterException.class)
    public void buyBook(String item, String address) {
        packageDispatcher.dispatch(item, address);
        invoicePrinter.print(orderId.getAndIncrement(), "Invoice body would go here, blah blah blah");

        //other activities, such as updating inventory and charging the customer
    }

    @Compensatable(value = CompensationTransactionType.MANDATORY, cancelOn = PackageDispatcherException.class,
            dontCancelOn = InvoicePrinterException.class)
    public void buyBookInCurrentTransaction(String item, String address) {
        packageDispatcher.dispatch(item, address);
        invoicePrinter.print(orderId.getAndIncrement(), "Invoice body would go here, blah blah blah");

        //other activities, such as updating inventory and charging the customer
    }

}
