package com.arjuna.jta.distributed.example.server.impl;

import java.util.ArrayList;
import java.util.List;

import javax.transaction.xa.XAResource;

import org.jboss.tm.XAResourceRecovery;

/**
 * This class is required by the transaction manager so that it can query the
 * list of running subordinate transactions which have it as a parent. Typically
 * we will know about these via the <code>Serializable</code>
 * <code>ProxyXAResource</code> class. However scenarios exist whereby the
 * subordinate has a prepared transaction and failed to return to the parent or
 * the parent failed itself. In this case we need to query these XIDs from the
 * remote server to detect these orphans.
 */
public class ProxyXAResourceRecovery implements XAResourceRecovery {

	private List<ProxyXAResource> resources = new ArrayList<ProxyXAResource>();

	public ProxyXAResourceRecovery(String nodeName, String[] toRecoverFor) {
		for (int i = 0; i < toRecoverFor.length; i++) {
			resources.add(new ProxyXAResource(nodeName, toRecoverFor[i]));
		}
	}

	@Override
	public XAResource[] getXAResources() {
		return resources.toArray(new XAResource[] {});
	}

}