package com.arjuna.jta.distributed.example.resources;

import jakarta.transaction.Synchronization;

/**
 * This is a simple Synchronization, any knowledge (such as the server name) it
 * has of the rest of the example is purely for debugging. It should be
 * considered a black box.
 */
public class TestSynchronization implements Synchronization {
	private String localServerName;

	public TestSynchronization(String localServerName) {
		this.localServerName = localServerName;
	}

	@Override
	public void beforeCompletion() {
		System.out.println(" TestSynchronization (" + localServerName + ")      beforeCompletion");
	}

	@Override
	public void afterCompletion(int status) {
		System.out.println(" TestSynchronization (" + localServerName + ")      afterCompletion");
	}
}