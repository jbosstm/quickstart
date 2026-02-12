package org.jboss.narayana.quickstarts.compensationsApi.travel;

import java.io.File;
import java.util.Date;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.narayana.quickstarts.compensationsApi.hotel.HotelServiceImpl;
import org.jboss.narayana.quickstarts.compensationsApi.taxi1.Taxi1ServiceImpl;
import org.jboss.narayana.quickstarts.compensationsApi.taxi2.Taxi2ServiceImpl;
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
public class TravelBookingTest {

    @Inject
    TravelBookingClient client;

    @Deployment
    public static WebArchive createTestArchive() {

        return ShrinkWrap.create(WebArchive.class, "test.war")
                .addPackages(true, HotelServiceImpl.class.getPackage().getName())
                .addPackages(true, Taxi1ServiceImpl.class.getPackage().getName())
                .addPackages(true, Taxi2ServiceImpl.class.getPackage().getName())
                .addPackages(true, TravelBookingTest.class.getPackage().getName())
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

        TravelBookingResult travelBookingResult = client.makeBooking("Paul", new Date(System.currentTimeMillis()));

        Assertions.assertTrue(travelBookingResult.getHotelBookingId() != null);
        Assertions.assertTrue(travelBookingResult.getTaxi1BookingId() == null);
        Assertions.assertTrue(travelBookingResult.getTaxi2BookingId() != null);
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