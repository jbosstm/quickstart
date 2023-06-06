package com.arjuna.xts.nightout.clients.jboss.taxi ;

import java.rmi.Remote;
import java.rmi.RemoteException ;

public interface ITaxiServiceAT extends Remote
{

    /**
     * Book a taxi.
     * @throws RemoteException for communication errors.
     */
    public void bookTaxi() throws RemoteException ;

}