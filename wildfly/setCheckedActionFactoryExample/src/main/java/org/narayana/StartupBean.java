/*
 * JBoss, Home of Professional Open Source
 * Copyright 2016, Red Hat, Inc., and others contributors as indicated
 * by the @authors tag. All rights reserved.
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
 */
package org.narayana;

import java.util.Hashtable;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.AccessTimeout;
import jakarta.ejb.EJB;
import jakarta.ejb.Singleton;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;

import org.narayana.handler.MyEJB;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.common.arjPropertyManager;
import com.arjuna.ats.arjuna.coordinator.CheckedAction;
import com.arjuna.ats.arjuna.coordinator.CheckedActionFactory;

@Singleton
@jakarta.ejb.Startup
@TransactionAttribute(TransactionAttributeType.NEVER)
@AccessTimeout(-1)
public class StartupBean {

	@EJB
	private MyEJB myEJB;

	@PostConstruct
	private void test() {
		System.out.println("StartupBean call");

		arjPropertyManager.getCoordinatorEnvironmentBean()
				.setCheckedActionFactory(new CheckedActionFactory() {

					@Override
					public CheckedAction getCheckedAction(Uid txId,
							String actionType) {
						System.out.println("MyEJB::getCheckedAction called");
						return new CheckedAction() {
							@Override
							public void check(boolean isCommit, Uid actUid,
									Hashtable list) {
								System.out.println("MyEJB::check called");
							}
						};
					}

				});
		myEJB.call();
		
	}

}
