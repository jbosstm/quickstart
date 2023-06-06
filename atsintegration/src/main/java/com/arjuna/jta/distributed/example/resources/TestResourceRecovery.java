package com.arjuna.jta.distributed.example.resources;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.transaction.xa.XAResource;

import org.jboss.tm.XAResourceRecovery;

/**
 * This is a simple TestResource XAResourceRecovery helper, any knowledge it has
 * of the rest of the example is purely for debugging. It should be considered a
 * black box.
 */
public class TestResourceRecovery implements XAResourceRecovery {

	private List<TestResource> resources = new ArrayList<TestResource>();

	public TestResourceRecovery(String nodeName) throws IOException {
		File file = new File(System.getProperty("user.dir") + "/distributedjta-examples/TestResource/" + nodeName + "/");
		if (file.exists() && file.isDirectory()) {
			File[] listFiles = file.listFiles();
			for (int i = 0; i < listFiles.length; i++) {
				File currentFile = listFiles[i];
				if (currentFile.getAbsolutePath().endsWith("_")) {
					resources.add(new TestResource(nodeName, currentFile));
				}
			}
		}
	}

	@Override
	public XAResource[] getXAResources() {
		return resources.toArray(new XAResource[] {});
	}

}