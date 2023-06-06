package org.narayana.handler;

import jakarta.ejb.Singleton;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;

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