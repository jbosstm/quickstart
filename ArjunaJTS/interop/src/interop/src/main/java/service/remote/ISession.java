package service.remote;

import javax.ejb.Remote;
import java.rmi.RemoteException;

//@Remote
public interface ISession extends javax.ejb.EJBObject {
    String getNext() throws RemoteException;

    String getNext(String failureType) throws RemoteException;
}
