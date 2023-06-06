package org.jboss.narayana.quickstart.spring.xa;

import org.jboss.tm.XAResourceRecovery;

import javax.transaction.xa.XAResource;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DummyXAResourceRecovery implements XAResourceRecovery {
    @Override
    public XAResource[] getXAResources() throws RuntimeException {
        List<DummyXAResource> resources = new ArrayList<DummyXAResource>();
        File file = new File("DummyXAResource/");
        if (file.exists() && file.isDirectory()) {
            File[] listFiles = file.listFiles();
            for (int i = 0; i < listFiles.length; i++) {
                File currentFile = listFiles[i];
                if (currentFile.getAbsolutePath().endsWith("_")) {
                    try {
                        resources.add(new DummyXAResource(currentFile));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    System.out.println("[" + Thread.currentThread().getName() + "] DummyXAResourceRecovery Added DummyXAResource: " + currentFile.getName());
                }
            }
        }
        System.out.println("[" + Thread.currentThread().getName() + "] DummyXAResourceRecovery returning list of DummyXAResources of length: " + resources.size());
        return resources.toArray(new XAResource[]{});
    }
}