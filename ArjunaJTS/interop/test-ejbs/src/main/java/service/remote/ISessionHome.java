package service.remote;

public interface ISessionHome extends javax.ejb.EJBHome {
   public service.remote.ISession create() throws java.rmi.RemoteException;
}
