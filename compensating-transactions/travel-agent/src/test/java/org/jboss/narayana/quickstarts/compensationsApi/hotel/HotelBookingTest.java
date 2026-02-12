package org.jboss.narayana.quickstarts.compensationsApi.hotel;

import java.io.File;
import java.util.Date;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.narayana.compensations.api.TransactionCompensatedException;
import org.jboss.narayana.quickstarts.compensationsApi.taxi1.Taxi1ServiceImpl;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.arjuna.mw.wst11.UserBusinessActivity;
import com.arjuna.mw.wst11.UserBusinessActivityFactory;

import jakarta.inject.Inject;

@ExtendWith(ArquillianExtension.class)
public class HotelBookingTest {

    @Inject
    HotelBookingClient client;

    @Deployment
    public static WebArchive createTestArchive() {

        return ShrinkWrap.create(WebArchive.class, "test.war")
                .addPackages(true, HotelServiceImpl.class.getPackage().getName())
                .addPackages(true, Taxi1ServiceImpl.class.getPackage().getName())
                .addPackages(true, HotelBookingTest.class.getPackage().getName())
                .addAsWebInfResource(new File("src/test/resources/persistence.xml"), "classes/META-INF/persistence.xml")
                .addAsWebInfResource(new StringAsset("<beans bean-discovery-mode=\"all\"></beans>"), "beans.xml");
    }


    /**
     * Test the simple scenario where a booking is made within a compensation based transaction which is completed successfully.
     *
     * @throws Exception if something goes wrong.
     */
    @Test
    public void testSuccess() throws Exception {

        client.makeBooking("Paul", new Date(System.currentTimeMillis()), false);

        BookingStatus bookingStatus = client.getLastBookingStatus();
        Assertions.assertTrue(bookingStatus.equals(BookingStatus.CONFIRMED), "Expected booking to be confirmed, but it wasn't: " + bookingStatus);
    }

    /**
     * Tests the scenario where a booking is made within a compensation-based transaction that is later cancelled. The
     * work should be compensated and thus the booking should end up in the state 'cancelled'.
     *
     * @throws Exception if something goes wrong
     */
    @Test
    public void testCancel() throws Exception {

        try {
            client.makeBooking("Paul", new Date(System.currentTimeMillis()), true);
            Assertions.fail("Should have thrown a TransactionCompensatedException");
        } catch (TransactionCompensatedException e) {
            //expected
        }

        Assertions.assertTrue(client.getLastBookingStatus().equals(BookingStatus.CANCELLED), "Expected booking to be cancelled, but it wasn't");

    }

    /**
     * Utility method for cancelling a Business Activity if it is currently active.
     */
    @AfterEach
    public void cancelIfActive() {

        try {
            UserBusinessActivity uba = UserBusinessActivityFactory.userBusinessActivity();
            uba.cancel();
        } catch (Throwable th2) {
            // do nothing, already closed
        }
    }


}