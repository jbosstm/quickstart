/*
 * JBoss, Home of Professional Open Source
 * Copyright 2016, Red Hat, Inc., and others contributors as indicated
 * by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */
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
