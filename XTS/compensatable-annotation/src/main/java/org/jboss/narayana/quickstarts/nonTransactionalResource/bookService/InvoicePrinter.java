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

import org.jboss.narayana.compensations.api.TxCompensate;
import org.jboss.narayana.compensations.api.TxConfirm;

import javax.inject.Inject;

/**
 * @author gytis@redhat.com 05/08/2013
 * @author paul.robinson@redhat.com 02/08/2013
 */
public class InvoicePrinter {

    @Inject
    InvoiceData invoiceData;

    public static boolean hasInk = true;

    @TxCompensate(DestroyInvoice.class)
    @TxConfirm(ConfirmInvoice.class)
    public void print(Integer invoiceId, String invoiceBody) {
        if (!hasInk) {
            throw new InvoicePrinterException("Printer has run out of ink. Unable to print invoice");
        }

        invoiceData.setInvoiceBody(invoiceBody);
        invoiceData.setInvoiceId(invoiceId);

        System.out.println("Printing the invoice ready for posting...");
    }

}
