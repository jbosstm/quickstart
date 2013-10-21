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
package org.jboss.narayana.quickstarts.compensationsApi.hotel;

import com.arjuna.mw.wst11.UserBusinessActivity;
import com.arjuna.mw.wst11.UserBusinessActivityFactory;
import junit.framework.Assert;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.narayana.compensations.api.TransactionCompensatedException;
import org.jboss.narayana.quickstarts.compensationsApi.taxi1.Taxi1ServiceImpl;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.util.Date;

@RunWith(Arquillian.class)
public class HotelBookingTest {

    @Inject
    HotelBookingClient client;

    private static final String ManifestMF = "Manifest-Version: 1.0\n"
            + "Dependencies: org.jboss.xts\n";

    @Deployment
    public static JavaArchive createTestArchive() {

        return ShrinkWrap.create(JavaArchive.class, "test.jar")
                .addPackages(true, HotelServiceImpl.class.getPackage().getName())
                .addPackages(true, Taxi1ServiceImpl.class.getPackage().getName())
                .addPackages(true, HotelBookingTest.class.getPackage().getName())
                .addAsManifestResource("persistence.xml")
                .addAsManifestResource(EmptyAsset.INSTANCE, ArchivePaths.create("beans.xml"))
                .setManifest(new StringAsset(ManifestMF));
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
        Assert.assertTrue("Expected booking to be confirmed, but it wasn't: " + bookingStatus, bookingStatus.equals(BookingStatus.CONFIRMED));
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
            Assert.fail("Should have thrown a TransactionCompensatedException");
        } catch (TransactionCompensatedException e) {
            //expected
        }

        Assert.assertTrue("Expected booking to be cancelled, but it wasn't", client.getLastBookingStatus().equals(BookingStatus.CANCELLED));

    }

    /**
     * Utility method for cancelling a Business Activity if it is currently active.
     */
    @After
    public void cancelIfActive() {

        try {
            UserBusinessActivity uba = UserBusinessActivityFactory.userBusinessActivity();
            uba.cancel();
        } catch (Throwable th2) {
            // do nothing, already closed
        }
    }


}
