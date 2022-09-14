/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
 * See the copyright.txt in the distribution for a full listing
 * of individual contributors.
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
 *
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
package org.jboss.narayana.quickstarts.nonTransactionalResource;

import java.io.File;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.narayana.quickstarts.nonTransactionalResource.bookService.BookService;
import org.jboss.narayana.quickstarts.nonTransactionalResource.bookService.InvoicePrinter;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import jakarta.inject.Inject;

@ExtendWith(ArquillianExtension.class)
@Disabled
// jakarta TODO: remove @Ignore and fix NullPointerException at deployment.test.war//org.jboss.narayana.quickstarts.nonTransactionalResource.BookServiceTest.testSuccess(BookServiceTest.java:70)
public class BookServiceTest {

    @Inject
    BookService bookService;

    @Deployment
    public static WebArchive createTestArchive() {

        WebArchive archive = ShrinkWrap.create(WebArchive.class, "test.war")
                .addPackages(true, BookService.class.getPackage().getName())
                .addAsWebInfResource(new File("src/main/webapp", "WEB-INF/beans.xml"));

        return archive;
    }

    @BeforeEach
    public void resetInk() {

        InvoicePrinter.hasInk = true;
    }

    @Test
    public void testSuccess() throws Exception {

        System.out.println("Running a test for the success case");

        bookService.buyBook("Java Transaction Processing: Design and Implementation", "paul.robinson@redhat.com");
    }

    @Test
    public void testFailure() throws Exception {

        System.out.println("Running a test for the failure case, where the printer has run out of ink");

        InvoicePrinter.hasInk = false;
        RuntimeException thrown = Assertions.assertThrows(RuntimeException.class, () -> {
            bookService.buyBook("Java Transaction Processing: Design and Implementation", "paul.robinson@redhat.com");
        });
    }


}
