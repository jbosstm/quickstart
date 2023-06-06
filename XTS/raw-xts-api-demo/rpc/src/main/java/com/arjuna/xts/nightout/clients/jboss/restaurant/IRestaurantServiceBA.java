package com.arjuna.xts.nightout.clients.jboss.restaurant ;

import java.rmi.Remote;
import java.rmi.RemoteException ;

public interface IRestaurantServiceBA extends Remote
{

    /**
     * Book a specified number of seats at the restaurant.
     * @param numSeats The number of seats to book at the restaurant.
     * @return true if successful, false otherwise
     * @throws RemoteException for communication errors.
     */
    public boolean bookSeats(final int numSeats) throws RemoteException ;

}