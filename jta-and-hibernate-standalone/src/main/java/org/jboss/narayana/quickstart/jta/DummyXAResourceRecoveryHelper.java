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