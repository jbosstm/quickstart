package com.arjuna.jta.distributed.example.server;

import java.io.IOException;

import jakarta.transaction.InvalidTransactionException;
import jakarta.transaction.NotSupportedException;
import jakarta.transaction.Synchronization;
import jakarta.transaction.SystemException;
import jakarta.transaction.TransactionManager;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import com.arjuna.ats.arjuna.common.CoreEnvironmentBeanException;

/**
 * This is the local interface of the server, operations invoked here should be
 * considered to be called on local objects. The are sat behind this interface
 * though in order to allow multiple copies of a server to be loaded into
 * memory.
 */
public interface LocalServer {

	/**
	 * Initialize this server, this will create a transaction manager service
	 * and a recovery manager service.
	 * 
	 * @param classLoaderForTransactionManager
	 *            This is the classloader that the transaction manager would
	 *            normally have access to.
	 * 
	 * @throws CoreEnvironmentBeanException
	 * @throws IOException
	 * @throws SecurityException
	 * @throws NoSuchFieldException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public void initialise(LookupProvider lookupProvider, String nodeName, int portOffset, String[] clusterCompatriots,
			ClassLoader classLoaderForTransactionManager) throws CoreEnvironmentBeanException, IOException, SecurityException, NoSuchFieldException,
			IllegalArgumentException, IllegalAccessException;

	/**
	 * Get the local transaction managers node name.
	 */
	public String getNodeName();

	/**
	 * Get a reference on the local transaction manager.
	 * 
	 * @return
	 * @throws NotSupportedException
	 * @throws SystemException
	 */
	public TransactionManager getTransactionManager() throws NotSupportedException, SystemException;

	/**
	 * Store the current transaction, this is so if a subordinate comes back
	 * here we have a hashmap to locate the transaction in.
	 * 
	 * Clearly servers where the transaction has been inflowed back to *must
	 * not* commit the transaction.
	 * 
	 * NOTE: CMT would not allow you do this anyway
	 * 
	 * @throws SystemException
	 */
	public void storeRootTransaction() throws SystemException;

	/**
	 * Remove the parent transaction from the local cache. It is indexed on XID.
	 * 
	 * @param toMigrate
	 */
	public void removeRootTransaction(Xid toMigrate);

	/**
	 * Either create or locate a subordinate (or root) transaction for a given
	 * Xid.
	 * 
	 * If it is the root transaction, it must not be committed!
	 * 
	 * NOTE: CMT would not allow you do this anyway
	 * 
	 * e.g. A transaction flowed 1,2,1 **must not** be committed at the third
	 * stage of the flow even though we are back at the originating server!!!
	 * 
	 * When a transaction is propagated to a server the transport is responsible
	 * for detecting that the server has not participated in the transaction yet
	 * and if so it must assign it the next available subordinate name and
	 * persist this information to help with recovery (see ServerImpl.java and
	 * the test itself for how to determine the next available subordinate name
	 * and a potential method of persisting this data). This is important when a
	 * proxy xa resource is involved in recovery and invokes commit or rollback
	 * as the transaction must be reloaded by the remote server before the
	 * commit/rollback – if it was prepared - before we attempt to complete the
	 * transaction.
	 * 
	 * @param remainingTimeout
	 * @param toImport
	 * @return
	 * @throws XAException
	 * @throws InvalidTransactionException
	 * @throws IllegalStateException
	 * @throws SystemException
	 * @throws IOException
	 */
	public Xid locateOrImportTransactionThenResumeIt(int remainingTimeout, Xid toImport) throws XAException, InvalidTransactionException,
			IllegalStateException, SystemException, IOException;

	/**
	 * Transport specific function to generate a proxy for a remote server.
	 * 
	 * @param remoteServerName
	 * 
	 * @return
	 * @throws IOException
	 * @throws SystemException
	 */
	public XAResource generateProxyXAResource(String remoteServerName, Xid migratedTransaction) throws SystemException;

	/**
	 * Generate a proxy synchronization
	 * 
	 * @param remoteServerName
	 * @param toRegisterAgainst
	 * 
	 * @return
	 */
	public Synchronization generateProxySynchronization(String remoteServerName, Xid toRegisterAgainst);

	/**
	 * Get the current Xid - this is what will be propagated to the remote
	 * servers.
	 * 
	 * @return
	 * @throws SystemException
	 */
	public Xid getCurrentXid() throws SystemException;

	/**
	 * Test code to create a reference of this server as a remote endpoint for
	 * other servers to communicate with.
	 * 
	 * @return
	 */
	public RemoteServer connectTo();

	/**
	 * This is used by the test to ensure that the servers classloader is set on
	 * a thread.
	 * 
	 * @return
	 */
	public ClassLoader getClassLoader();
}