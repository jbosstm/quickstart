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
package org.jboss.narayana.quickstarts.nonTransactionalResource;

import com.arjuna.mw.wst11.UserBusinessActivityFactory;
import junit.framework.Assert;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.narayana.compensations.api.TransactionRequiredException;
import org.jboss.narayana.compensations.api.TransactionalException;
import org.jboss.narayana.compensations.impl.CompensationManagerImpl;
import org.jboss.narayana.compensations.impl.CompensationManagerState;
import org.jboss.narayana.quickstarts.nonTransactionalResource.bookService.BookService;
import org.jboss.narayana.quickstarts.nonTransactionalResource.bookService.ConfirmInvoice;
import org.jboss.narayana.quickstarts.nonTransactionalResource.bookService.ConfirmPackage;
import org.jboss.narayana.quickstarts.nonTransactionalResource.bookService.DestroyInvoice;
import org.jboss.narayana.quickstarts.nonTransactionalResource.bookService.InvoicePrinter;
import org.jboss.narayana.quickstarts.nonTransactionalResource.bookService.InvoicePrinterException;
import org.jboss.narayana.quickstarts.nonTransactionalResource.bookService.PackageDispatcher;
import org.jboss.narayana.quickstarts.nonTransactionalResource.bookService.PackageDispatcherException;
import org.jboss.narayana.quickstarts.nonTransactionalResource.bookService.RecallPackage;
import org.jboss.narayana.txframework.impl.TXDataMapImpl;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.util.HashMap;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
@RunWith(Arquillian.class)
public class BookServiceTest {

    private static final String ManifestMF = "Manifest-Version: 1.0\n"
            + "Dependencies: org.jboss.xts,org.jboss.xts,org.jboss.msc,org.jboss.jts\n";

    @Inject
    BookService bookService;

    @Inject
    ConfirmPackage confirmPackage;

    @Inject
    ConfirmInvoice confirmInvoice;

    @Inject
    DestroyInvoice destroyInvoice;

    @Inject
    RecallPackage recallPackage;

    @Deployment
    public static WebArchive createTestArchive() {

        return ShrinkWrap.create(WebArchive.class, "test.war")
                .addPackages(true, BookService.class.getPackage().getName())
                .addAsWebInfResource(EmptyAsset.INSTANCE, ArchivePaths.create("beans.xml"))
                .setManifest(new StringAsset(ManifestMF));
    }

    @Before
    public void before() {
        InvoicePrinter.hasInk = true;
        PackageDispatcher.availableItems.add("Java Transaction Processing: Design and Implementation");
        confirmPackage.reset();
        confirmInvoice.reset();
        destroyInvoice.reset();
        recallPackage.reset();
    }

    @Test
    public void testSuccess() throws Exception {
        System.out.println("Running a test for the success case");

        bookService.buyBook("Java Transaction Processing: Design and Implementation", "gytis@redhat.com");
        Assert.assertTrue(confirmInvoice.wasInvoicePrinted());
        Assert.assertTrue(confirmPackage.wasPackageDispached());
    }

    @Test
    public void testBookItemWithoutPrinting() {
        System.out.println("Running a test with successful booking but without printing the invoice");

        InvoicePrinter.hasInk = false;

        try {
            bookService.buyBook("Java Transaction Processing: Design and Implementation", "gytis@redhat.com");
            Assert.fail("InvoicePrinterException was expected.");
        } catch (InvoicePrinterException e) {
            // Expected
        }

        Assert.assertTrue(confirmPackage.wasPackageDispached());
        Assert.assertFalse(confirmInvoice.wasInvoicePrinted());
    }

    @Test
    public void testFailureToDispach() throws Exception {
        System.out.println("Running a test for the failure case, where requested item does not exist");

        try {
            bookService.buyBook("Java Transaction Design Strategies", "gytis@redhat.com");
            Assert.fail("PackageDispatcherException was expected.");
        } catch (PackageDispatcherException e) {
            // Expected
        }

        Assert.assertFalse(confirmPackage.wasPackageDispached());
        Assert.assertFalse(confirmInvoice.wasInvoicePrinted());
    }

    @Test
    public void testWithMissingMandatoryTransaction() {
        System.out.println("Running a test for the failure case, where mandatory transaction is missing");

        try {
            bookService.buyBookInCurrentTransaction("Java Transaction Processing: Design and Implementation",
                    "gytis@redhat.com");
            Assert.fail("TransactionalException was expected.");
        } catch (TransactionalException e) {
            Assert.assertTrue(e.getCause() instanceof TransactionRequiredException);
        }
    }

    @Test
    public void testFailureToDispachSecondItem() throws Exception {
        System.out.println("Running a test for the failure case, where second item does not exist");

        beginBusinessActivity();

        try {
            bookService.buyBookInCurrentTransaction("Java Transaction Processing: Design and Implementation",
                    "gytis@redhat.com");
            bookService.buyBookInCurrentTransaction("Java Transaction Design Strategies", "gytis@redhat.com");
            Assert.fail("PackageDispatcherException was expected.");
        } catch (PackageDispatcherException e) {
            // Expected
        }

        final boolean result = completeBusinessActivity();
        Assert.assertFalse("Transaction was expected to be canceled.", result);
        Assert.assertTrue(recallPackage.wasPackageRecalled()); // First package was recalled after failure.
        Assert.assertTrue(destroyInvoice.wasInvoiceDestroyed()); // First package was destroyed after failure.
    }

    private void beginBusinessActivity() throws Exception {
        UserBusinessActivityFactory.userBusinessActivity().begin();
        CompensationManagerImpl.resume(new CompensationManagerState());
        TXDataMapImpl.resume(new HashMap());
    }

    /**
     * Completes business activity.
     *
     * @return boolean True if activity was closed successfully and False if activity was canceled.
     */
    private boolean completeBusinessActivity() throws Exception {
        if (CompensationManagerImpl.isCompensateOnly()) {
            UserBusinessActivityFactory.userBusinessActivity().cancel();
            CompensationManagerImpl.suspend();
            TXDataMapImpl.suspend();

            return false;
        } else {
            UserBusinessActivityFactory.userBusinessActivity().close();
            CompensationManagerImpl.suspend();
            TXDataMapImpl.suspend();

            return true;
        }
    }

}
