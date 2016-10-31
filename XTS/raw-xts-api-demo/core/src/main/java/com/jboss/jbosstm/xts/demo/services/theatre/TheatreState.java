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
package com.jboss.jbosstm.xts.demo.services.theatre;

import com.jboss.jbosstm.xts.demo.services.state.ServiceState;

import java.util.Arrays;

import static com.jboss.jbosstm.xts.demo.services.theatre.TheatreConstants.*;

/**
 * An object which models the state of a restaurant identifying the number of free and
 * booked seats and the total available
 */
public class TheatreState extends ServiceState {
    int[] totalSeats;
    int[] bookedSeats;
    int[] freeSeats;

    /**
     * create a new initial theatre state
     * @return an initial theatre state containing no booked seats
     */
    public static TheatreState initialState()
    {
        return new TheatreState();
    }
    /**
     * derive a child theatre state from this state
     * @return a derived theatre state containing the same data as this state
     * but having a version id one greater 
     */
    public TheatreState derivedState()
    {
        return new TheatreState(this);
    }

    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("TheatreState{version=");
        builder.append(version);
        builder.append(", totalSeats=[circle=");
        builder.append(totalSeats[CIRCLE]);
        builder.append(", STALLS=");
        builder.append(totalSeats[STALLS]);
        builder.append(", BALCONY=");
        builder.append(totalSeats[BALCONY]);
        builder.append("], bookedSeats=[circle=");
        builder.append(bookedSeats[CIRCLE]);
        builder.append(", STALLS=");
        builder.append(bookedSeats[STALLS]);
        builder.append(", BALCONY=");
        builder.append(bookedSeats[BALCONY]);
        builder.append("], freeSeats=[circle=");
        builder.append(freeSeats[CIRCLE]);
        builder.append(", STALLS=");
        builder.append(freeSeats[STALLS]);
        builder.append(", BALCONY=");
        builder.append(freeSeats[BALCONY]);
        builder.append("]}");
        return builder.toString();
    }
    /**
     * create a new initial restaurant state
     */
    private TheatreState()
    {
        this.totalSeats = new int[NUM_SEAT_AREAS];
        this.bookedSeats = new int[NUM_SEAT_AREAS];
        this.freeSeats = new int[NUM_SEAT_AREAS];
        for (int i = 0; i < NUM_SEAT_AREAS; i++) {
            totalSeats[i] = DEFAULT_SEATING_CAPACITY;
            bookedSeats[i] = 0;
            freeSeats[i] = DEFAULT_SEATING_CAPACITY;
        }
    }

    /**
     * create a theatre state derived from a parent state
     *
     * @param parent the parent state whose data should be copied into this state
     * and whose version should be incremented by 1 and then installed in this state.
     */
    private TheatreState(TheatreState parent)
    {
        super(parent);
        this.totalSeats =  Arrays.copyOf(parent.totalSeats, NUM_SEAT_AREAS);
        this.bookedSeats =  Arrays.copyOf(parent.bookedSeats, NUM_SEAT_AREAS);
        this.freeSeats =  Arrays.copyOf(parent.freeSeats, NUM_SEAT_AREAS);
    }

}
