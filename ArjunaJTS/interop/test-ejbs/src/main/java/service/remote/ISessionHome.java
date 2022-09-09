package service.remote;

public interface ISessionHome extends jakarta.ejb.EJBHome {
   public service.remote.ISession create() throws java.rmi.RemoteException;
}
