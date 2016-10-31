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
package org.jboss.narayana.quickstarts.compensationsApi.hotel;

import org.jboss.narayana.compensations.api.CompensationScoped;

import java.io.Serializable;

/**
 * This bean is used to store state in the business method and make it available when the confirmation and compensation
 * handlers are invoked.
 *
 * @author paul.robinson@redhat.com 02/08/2013
 */
@CompensationScoped
public class BookingData implements Serializable {

    Integer bookingId;

    public Integer getBookingId() {

        return bookingId;
    }

    public void setBookingId(Integer bookingId) {

        this.bookingId = bookingId;
    }
}
