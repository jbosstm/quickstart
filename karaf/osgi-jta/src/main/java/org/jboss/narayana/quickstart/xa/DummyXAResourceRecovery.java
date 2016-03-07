/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.narayana.quickstart.xa;

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
            for(File currentFile : file.listFiles()) {
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

        if (resources.size() > 0) {
            System.out.println("[" + Thread.currentThread().getName() + "] DummyXAResourceRecovery returning list of DummyXAResources of length: " + resources.size());
        }
        return resources.toArray(new XAResource[]{});
    }
}
