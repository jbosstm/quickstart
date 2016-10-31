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

/**
 * Constant values used by the theatre service
 */
public class TheatreConstants
{
    /**
     * Constant (array index) used for the seating area CIRCLE.
     */
    public static final int CIRCLE = 0;

    /**
     * Constant (array index) used for the seating area STALLS.
     */
    public static final int STALLS = 1;

    /**
     * Constant (array index) used for the seating area BALCONY.
     */
    public static final int BALCONY = 2;

    /**
     * The total number (array size) of seating areas.
     */
    public static final int NUM_SEAT_AREAS = 3;

    /**
     * The default initial capacity of each seating area.
     */
    public static final int DEFAULT_SEATING_CAPACITY = 100;

    /**
     * the name of the file used to store the current theatre manager state
     */
    final static public String STATE_FILENAME = "theatreManagerState";

    /**
     * the name of the file used to store the shadow theatre manager state
     */
    final static public String SHADOW_STATE_FILENAME = "theatreManagerShadowState";
}
