package org.jboss.narayana.quickstarts.nonTransactionalResource;

import java.io.File;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.narayana.quickstarts.nonTransactionalResource.bookService.BookService;
import org.jboss.narayana.quickstarts.nonTransactionalResource.bookService.InvoicePrinter;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import jakarta.inject.Inject;

@ExtendWith(ArquillianExtension.class)
public class BookServiceTest {

    @Inject
    BookService bookService;

    @Deployment
    public static WebArchive createTestArchive() {

        WebArchive archive = ShrinkWrap.create(WebArchive.class, "test.war")
                .addPackages(true, BookService.class.getPackage().getName())
                .addAsWebInfResource(new StringAsset("<beans bean-discovery-mode=\"all\"></beans>"), "beans.xml");

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