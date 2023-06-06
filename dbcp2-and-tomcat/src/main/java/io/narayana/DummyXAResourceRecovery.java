package io.narayana;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import javax.transaction.xa.XAResource;

import org.jboss.tm.XAResourceRecovery;

/**
 * This class is used solely for simulating system crash.
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 * @author <a href="mailto:zfeng@redhat.com">Amos Feng</a>
 */
public class DummyXAResourceRecovery implements XAResourceRecovery {

    @Override
    public XAResource[] getXAResources() throws RuntimeException {
        List<DummyXAResource> resources;
        try {
            resources = getXAResourcesFromDirectory(DummyXAResource.LOG_DIR);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.out.println(DummyXAResourceRecovery.class.getSimpleName() + " returning list of resources: " + resources);

        return resources.toArray(new XAResource[]{});
    }

    private List<DummyXAResource> getXAResourcesFromDirectory(String directory) throws IOException {
        List<DummyXAResource> resources = new ArrayList<>();

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