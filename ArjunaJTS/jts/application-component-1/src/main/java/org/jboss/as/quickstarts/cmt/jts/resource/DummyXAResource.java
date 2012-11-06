package org.jboss.as.quickstarts.cmt.jts.resource;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

public class DummyXAResource implements XAResource {

	private String name;

	public DummyXAResource(String name) {
		this.name = name;
	}

	@Override
	public void commit(Xid arg0, boolean arg1) throws XAException {
//		System.out.println(name + "commited");
//		Thread.dumpStack();
		// try {
		// Thread.currentThread().sleep(60000);
		// } catch (InterruptedException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
	}

	@Override
	public void end(Xid arg0, int arg1) throws XAException {
		// System.out.println(name + "end");
	}

	@Override
	public void forget(Xid arg0) throws XAException {
		// System.out.println(name + "forget");
	}

	@Override
	public int getTransactionTimeout() throws XAException {
		// System.out.println(name + "getTransactionTimeout");
		return 0;
	}

	@Override
	public boolean isSameRM(XAResource arg0) throws XAException {
		// System.out.println(name + "isSameRM");
		return this == arg0;
	}

	@Override
	public int prepare(Xid arg0) throws XAException {
//		System.out.println(name + "prepare");
		return 0;
	}

	@Override
	public Xid[] recover(int arg0) throws XAException {
		// System.out.println(name + "recover");
		return null;
	}

	@Override
	public void rollback(Xid arg0) throws XAException {
//		System.out.println(name + "rollback");
	}

	@Override
	public boolean setTransactionTimeout(int arg0) throws XAException {
		// System.out.println(name + "setTransactionTimeout");
		return false;
	}

	@Override
	public void start(Xid arg0, int arg1) throws XAException {
		// System.out.println(name + "start");
	}
}
