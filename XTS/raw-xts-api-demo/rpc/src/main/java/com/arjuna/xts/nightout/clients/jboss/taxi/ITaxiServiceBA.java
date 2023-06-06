package com.arjuna.xts.nightout.clients.jboss.taxi ;

import java.rmi.Remote;
import java.rmi.RemoteException ;

public interface ITaxiServiceBA extends Remote
{

    /**
     * Book a taxi.
     * @return true if successful, false otherwise.
     * @throws RemoteException for communication errors.
     */
    public boolean bookTaxi() throws RemoteException ;

}