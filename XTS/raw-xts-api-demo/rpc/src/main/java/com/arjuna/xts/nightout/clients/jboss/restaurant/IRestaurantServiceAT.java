package com.arjuna.xts.nightout.clients.jboss.restaurant ;

import java.rmi.Remote;
import java.rmi.RemoteException ;

public interface IRestaurantServiceAT extends Remote
{

    /**
     * Book a specified number of seats at the restaurant.
     * @param numSeats The number of seats to book at the restaurant.
     * @throws RemoteException for communication errors.
     */
    public void bookSeats(final int numSeats) throws RemoteException ;

}