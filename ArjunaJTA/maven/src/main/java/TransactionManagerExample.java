/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and/or its affiliates,
 * and individual contributors as indicated by the @author tags.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 *
 * (C) 2011,
 * @author JBoss, by Red Hat.
 */
import jakarta.transaction.TransactionManager;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import com.arjuna.ats.arjuna.common.arjPropertyManager;

/**
 * This example shows how you can access the transaction manager.
 */
public class TransactionManagerExample {

	/**
	 * Get a reference to the transaction manager and call begin and rollback on
	 * it.
	 * 
	 * @param args
	 *            The command line arguments.
	 * @throws Exception
	 *             In case of error.
	 */
	public static void main(String[] args) throws Exception {
		arjPropertyManager.getCoreEnvironmentBean().setNodeIdentifier("1");
		TransactionManager tm = com.arjuna.ats.jta.TransactionManager
				.transactionManager();

		tm.begin();
		System.err.println(tm.getTransaction());
		tm.getTransaction().enlistResource(new XAResource() {
			public void commit(Xid arg0, boolean arg1) throws XAException {
			}

			public void end(Xid arg0, int arg1) throws XAException {
			}

			public void forget(Xid arg0) throws XAException {
			}

			public int getTransactionTimeout() throws XAException {
				return 0;
			}

			public boolean isSameRM(XAResource arg0) throws XAException {
				return false;
			}

			public int prepare(Xid arg0) throws XAException {
				return 0;
			}

			public Xid[] recover(int arg0) throws XAException {
				return null;
			}

			public void rollback(Xid arg0) throws XAException {
			}

			public boolean setTransactionTimeout(int arg0) throws XAException {
				return false;
			}

			public void start(Xid arg0, int arg1) throws XAException {
			}
			
		});

		tm.rollback();
		System.err.println(tm.getTransaction());
	}

}
