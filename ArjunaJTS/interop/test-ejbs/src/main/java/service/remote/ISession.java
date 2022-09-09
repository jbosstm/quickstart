package service.remote;

import jakarta.ejb.Remote;
import java.rmi.RemoteException;

//@Remote
public interface ISession extends jakarta.ejb.EJBObject {
    String getNext() throws RemoteException;

    String getNext(String failureType) throws RemoteException;
}
