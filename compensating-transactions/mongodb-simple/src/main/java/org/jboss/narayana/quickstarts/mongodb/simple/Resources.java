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