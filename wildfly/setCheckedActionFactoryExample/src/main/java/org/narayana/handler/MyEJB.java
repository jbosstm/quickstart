package org.narayana.handler;

import javax.ejb.Singleton;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import com.arjuna.ats.arjuna.coordinator.BasicAction;

@Singleton
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
public class MyEJB {

	public void call() {
		System.out.println("MyEJB call");
		Thread t = new Thread();
		BasicAction.Current().addChildThread(t);
	}
}
