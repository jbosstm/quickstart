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

public class SimpleResource implements XAResource {
	static final Logger logger = LogManager.getLogger(SimpleResource.class);

	public void commit(Xid xid, boolean onePhase) throws XAException {
		logger.info("SimpleResource commit called: {}", xid);
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
		logger.info("SimpleResource prepare called: {}", xid);
		return XA_OK;
	}

	public Xid[] recover(int flag) throws XAException {
		return null;
	}

	public void rollback(Xid xid) throws XAException {
		logger.info("SimpleResource unexpected ROLLBACK called: {}", xid);
		throw new XAException("SimpleResource unexpected ROLLBACK called");
	}

	public boolean setTransactionTimeout(int seconds) throws XAException {
		return true;
	}

	public void start(Xid xid, int flags) throws XAException {
	}

}