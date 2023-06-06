package com.arjuna.xts.nightout.clients.jboss.theatre ;

import java.rmi.Remote;
import java.rmi.RemoteException ;

public interface ITheatreServiceBA extends Remote
{

    /**
     * Book a specified number of seats in a specific area of the theatre.
     * @param numSeats The number of seats to book at the theatre.
     * @param area The area of the seats.
     * @return true if successful, false otherwise.
     * @throws RemoteException for communication errors.
     */
    public boolean bookSeats(final int numSeats, final int area)
            throws RemoteException ;

}