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

import org.jboss.narayana.compensations.api.CompensationHandler;

import javax.inject.Inject;

/**
 * @author gytis@redhat.com 05/08/2013
 * @author paul.robinson@redhat.com 02/08/2013
 */
public class DestroyInvoice implements CompensationHandler {

    private static boolean invoiceDestroyed = false;

    @Inject
    InvoiceData invoiceData;

    @Override
    public void compensate() {
        //Recall the package somehow
        System.out.println("Hunt down invoice with id '" + invoiceData.getInvoiceId() + "' and destroy it...");

        invoiceDestroyed = true;
    }

    public boolean wasInvoiceDestroyed() {
        return invoiceDestroyed;
    }

    public void reset() {
        invoiceDestroyed = false;
    }

}

