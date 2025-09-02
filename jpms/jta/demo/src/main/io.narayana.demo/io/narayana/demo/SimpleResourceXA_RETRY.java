/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package io.narayana.demo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

public class SimpleResourceXA_RETRY implements XAResource {
	static final Logger logger = LogManager.getLogger(SimpleResourceXA_RETRY.class);

	private Xid xid;
	private boolean firstAttemptToCommit = true;
	private boolean committed = false;
	private final Demonstration toWakeUp;

	public SimpleResourceXA_RETRY(Demonstration toWakeUp) {
		this.toWakeUp = toWakeUp;
	}

	public void commit(Xid xid, boolean onePhase) throws XAException {
		logger.info("SimpleResourceXA_RETRY commit called: {}", xid);
		if (firstAttemptToCommit) {
			firstAttemptToCommit = false;
			logger.warn("Returning XA_RETRY first time");
			throw new XAException(XAException.XA_RETRY);
		}
		xid = null;
		committed = true;
		synchronized (toWakeUp) {
			toWakeUp.committed();
		}
	}

	public void end(Xid xid, int flags) throws XAException {
	}

	public void forget(Xid xid) throws XAException {
	}

	public int getTransactionTimeout() throws XAException {
		return 0;
	}

	public boolean isSameRM(XAResource xar) throws XAException {
		return false;
	}

	public int prepare(Xid xid) throws XAException {
		logger.info("SimpleResourceXA_RETRY prepare called: {}", xid);
		this.xid = xid;
		return XA_OK;
	}

	public Xid[] recover(int flag) throws XAException {
		if (xid != null) {
			return new Xid[] { xid };
		}
		return null;
	}

	public void rollback(Xid xid) throws XAException {
		logger.warn("SimpleResourceXA_RETRY unexpected ROLLBACK called: {}", xid);
		throw new XAException("SimpleResourceXA_RETRY unexpected ROLLBACK");
	}

	public boolean setTransactionTimeout(int seconds) throws XAException {
		return true;
	}

	public void start(Xid xid, int flags) throws XAException {
	}

	public boolean wasCommitted() {
		return committed;
	}

}