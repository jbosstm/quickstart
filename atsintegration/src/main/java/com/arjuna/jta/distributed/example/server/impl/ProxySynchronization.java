package com.arjuna.jta.distributed.example.server.impl;

import jakarta.transaction.Synchronization;
import jakarta.transaction.SystemException;
import javax.transaction.xa.Xid;

import com.arjuna.jta.distributed.example.server.LookupProvider;

/**
 * An example to show how the transport can register a proxy synchronization.
 * 
 * <p>
 * Note that we do not proxy the afterCompletion, this is left to run locally
 * per subordinate.
 */
public class ProxySynchronization implements Synchronization {

	private String localServerName;
	private String remoteServerName;
	private Xid toRegisterAgainst;

	public ProxySynchronization(String localServerName, String remoteServerName, Xid toRegisterAgainst) {
		this.localServerName = localServerName;
		this.remoteServerName = remoteServerName;
		this.toRegisterAgainst = toRegisterAgainst;
	}

	/**
	 * Propagate the before completion in a transport specific manner.
	 */
	@Override
	public void beforeCompletion() {
		System.out.println("ProxySynchronization (" + localServerName + ":" + remoteServerName + ") beforeCompletion");
		try {
			LookupProvider.getInstance().lookup(remoteServerName).beforeCompletion(toRegisterAgainst);
		} catch (SystemException e) {
			// Unfortunately we cannot do much else here
			e.printStackTrace();
		}
	}

	@Override
	public void afterCompletion(int status) {
		// These are not proxied but are handled during local commits
	}
}