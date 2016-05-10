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
package org.jboss.narayana.quickstart.jta;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import javax.transaction.xa.XAResource;

import com.arjuna.ats.jta.recovery.XAResourceRecoveryHelper;

/**
 * A recovery helper class used to recover DummyXAResource. It reads logged XA resources from the object store and gives them
 * to the recovery manager.
 *
 * This quickstart's user shouldn't be interested in this class implementation details. Without it application's log would be
 * full of warnings from the recovery manager.
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 * @author <a href="mailto:zfeng@redhat.com">Amos Feng</a>
 */
public class DummyXAResourceRecoveryHelper implements XAResourceRecoveryHelper {

    @Override
    public boolean initialise(String p) throws Exception {
        return false;
    }

    @Override
    public XAResource[] getXAResources() throws RuntimeException {
        List<DummyXAResource> resources;
        try {
            resources = getXAResourcesFromDirectory(DummyXAResource.LOG_DIR);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.out.println(DummyXAResourceRecoveryHelper.class.getSimpleName() + " returning list of resources: " + resources);

        return resources.toArray(new XAResource[]{});
    }

    private List<DummyXAResource> getXAResourcesFromDirectory(String directory) throws IOException {
        List<DummyXAResource> resources = new ArrayList<>();

        if (!Files.exists(FileSystems.getDefault().getPath(directory))) {
            return resources;
        }

        Files.newDirectoryStream(FileSystems.getDefault().getPath(directory), "*_").forEach(path -> {
            try {
                resources.add(new DummyXAResource(path.toFile()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        return resources;
    }

}
