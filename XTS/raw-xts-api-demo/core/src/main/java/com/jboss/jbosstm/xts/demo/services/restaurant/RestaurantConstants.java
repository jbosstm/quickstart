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

/**
 * Constant values used by the restaurant service
 */
public class RestaurantConstants
{
    /**
     * The default initial capacity of each seating area.
     */
    public static final int DEFAULT_SEATING_CAPACITY = 100;

    /**
     * the name of the file used to persist the current restaurant state
     */
    public final static String STATE_FILENAME = "restaurantManagerState";

    /**
     * the name of the file used to persist the shadow restaurant state
     */
    public final static String SHADOW_STATE_FILENAME = "restaurantManagerShadowState";

}
