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
package org.jboss.narayana.quickstarts.mongodb.simple;

import com.mongodb.DB;
import com.mongodb.MongoClient;

import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.spi.InjectionPoint;

/**
 * A simple class for managing the lifecycle of the MongoDB connection and making it available for CDI injection.
 *
 * @author paul.robinson@redhat.com 12/04/2014
 */
public class Resources {

    /**
     * This producer method acts as a factory for creating DB instances used to interact with the MongoDB server.
     *
     * WARNING: this pattern is not recommended as creates a new connection for every injection point and never cleans
     * up connections.
     *
     * @param injectionPoint
     * @return
     */
    @Produces
    public DB produceDB(InjectionPoint injectionPoint) {
        MongoClient mongo = new MongoClient("localhost", 27017);
        return mongo.getDB("test");
    }

}
