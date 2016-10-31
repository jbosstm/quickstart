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
package com.jboss.jbosstm.xts.demo.services.restaurant;

import com.jboss.jbosstm.xts.demo.services.state.ServiceState;
import static com.jboss.jbosstm.xts.demo.services.restaurant.RestaurantConstants.*;

/**
 * An object which models the state of a restaurant identifying the number of free and
 * booked seats and the total available
 */
public class RestaurantState extends ServiceState {
    int totalSeats;
    int bookedSeats;
    int freeSeats;

    /**
     * create a new initial restaurant state
     * @return an initial restaurant state containing no booked seats
     */
    public static RestaurantState initialState()
    {
        return new RestaurantState();
    }

    /**
     * derive a child restaurant state from this state
     * @return a derived restaurant state containing the same data as this state
     * but having a version id one greater
     */
    public RestaurantState derivedState()
    {
        return new RestaurantState(this);
    }

    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("RestaurantState{version=");
        builder.append(version);
        builder.append(", totalSeats=");
        builder.append(totalSeats);
        builder.append(", bookedSeats=");
        builder.append(bookedSeats);
        builder.append(", freeSeats=");
        builder.append(freeSeats);
        builder.append("}");
        return builder.toString();
    }
    /**
     * create a new initial restaurant state
     */
    private RestaurantState()
    {
        super();
        this.totalSeats = DEFAULT_SEATING_CAPACITY;
        this.bookedSeats = 0;
        this.freeSeats = DEFAULT_SEATING_CAPACITY;
    }

    /**
     * create a restaurant state derived from a parent state
     *
     * @param parent the parent state whose data should be copied into this state
     * and whose version should be incremented by 1 and then installed in this state.
     */
    private RestaurantState(RestaurantState parent)
    {
        super(parent);
        this.totalSeats = parent.totalSeats;
        this.bookedSeats = parent.bookedSeats;
        this.freeSeats = totalSeats - bookedSeats;
    }

}
