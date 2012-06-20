/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005-2010, Red Hat, and individual contributors
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
 * @author JBoss Inc.
 */
package org.jboss.narayana.quickstarts.restat.taxi;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class represents the back-end resource for managing taxi bookings.
 * 
 * This is a mock implementation that just keeps a counter of how many bookings have been made.
 * 
 * @author paul.robinson@redhat.com, 2012-01-04
 */
public class MockTaxiManager {

    // The singleton instance of this class.
    private static MockTaxiManager singletonInstance;

    // A thread safe booking counter
    private AtomicInteger bookings = new AtomicInteger(0);

    /**
     * Accessor to obtain the singleton taxi manager instance.
     * 
     * @return the singleton TaxiManager instance.
     */
    public synchronized static MockTaxiManager getSingletonInstance() {
        if (singletonInstance == null) {
            singletonInstance = new MockTaxiManager();
        }

        return singletonInstance;
    }

    /**
     * Make a booking with the taxi.
     *
     * @return the iD of the booking
     */
    public synchronized String makeBooking() {
        System.out.println("[SERVICE] makeBooking called on backend resource.");
        return UUID.randomUUID().toString();
    }

    /**
     * Prepare local state changes for the supplied transaction. This method should persist any required information to ensure
     * that it can undo (rollback) or make permanent (commit) the work done inside this transaction, when told to do so.
     * 
     * @param bookingId The transaction identifier
     * @return true on success, false otherwise
     */
    public boolean prepare(String bookingId) {
        System.out.println("[SERVICE] prepare called on backend resource.");
        return true;
    }

    /**
     * commit local state changes for the supplied transaction
     * 
     * @param bookingId The transaction identifier
     */
    public void commit(String bookingId) {
        System.out.println("[SERVICE] commit called on backend resource.");
        bookings.getAndIncrement();
    }

    /**
     * roll back local state changes for the supplied transaction
     * 
     * @param bookingId The transaction identifier
     */
    public void rollback(String bookingId) {
        System.out.println("[SERVICE] rollback called on backend resource.");
    }

    /**
     * Returns the number of bookings
     * 
     * @return the number of bookings.
     */
    public int getBookingCount() {
        return bookings.get();
    }

    /**
     * Reset the booking counter to zero
     */
    public void reset() {
        bookings.set(0);
    }
}
