package com.arjuna.jta.distributed.example.server;

import jakarta.transaction.SystemException;
import javax.transaction.xa.XAException;
import javax.transaction.xa.Xid;

/**
 * This interface attempts to illustrate the network endpoint requirements of
 * the remote transaction server with regards subordinate transactions.
 * 
 * <p>
 * Many of the methods are required to provide a recover parameter, this is
 * because when using Serializable ProxyXAResources, the recover method is not
 * invoked and therfore the remote server will not have had chance to recover
 * the subordinate transactions.
 */
public interface RemoteServer {

	/**
	 * Prepare the subordinate transaction
	 * 
	 * @param xid
	 * @param recover
	 * @return
	 * @throws XAException
	 */
	public int prepare(Xid xid, boolean recover) throws XAException;

	/**
	 * Commit the subordinate transaction.
	 * 
	 * @param xid
	 * @param onePhase
	 * @param recover
	 * @throws XAException
	 */
	public void commit(Xid xid, boolean onePhase, boolean recover) throws XAException;

	/**
	 * Rollback the subordinate transaction.
	 * 
	 * @param xid
	 * @param recover
	 * @throws XAException
	 */
	public void rollback(Xid xid, boolean recover) throws XAException;

	/**
	 * Forget a subordinate transaction.
	 * 
	 * @param xid
	 * @param recover
	 * @throws XAException
	 */
	public void forget(Xid xid, boolean recover) throws XAException;

	/**
	 * Proxy synchronizations will need to invoke this.
	 * 
	 * @param xid
	 * @throws SystemException
	 */
	public void beforeCompletion(Xid xid) throws SystemException;

	/**
	 * This is used by the ProxyXAResourceRecovery helper class to detect
	 * orphaned subordinate transactions.
	 * 
	 * @param localServerName
	 * @return
	 * @throws XAException
	 */
	public Xid[] recoverFor(String localServerName) throws XAException;

}