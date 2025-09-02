package io.narayana.recovery;

import com.arjuna.ats.jta.recovery.XAResourceRecoveryHelper;
import io.narayana.config.Config;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.transaction.xa.XAResource;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class BasicTest {
    static Config config;

    @BeforeClass
    public static void beforeClass() {
        config = new Config(
                "1",
                "target",
                1,
                1,
                List.of(
                        "com.arjuna.ats.internal.arjuna.recovery.AtomicActionRecoveryModule",
                        "com.arjuna.ats.internal.jta.recovery.arjunacore.XARecoveryModule")
                );
    }

    @Test
    public void testRecovery() {
        Recovery recovery = new Recovery(config);
        AtomicBoolean helperCalled = new AtomicBoolean(false);
        XAResourceRecoveryHelper helper = new XAResourceRecoveryHelper() {
            @Override
            public boolean initialise(String p) throws Exception {
                return true;
            }

            @Override
            public XAResource[] getXAResources() throws Exception {
                helperCalled.set(true);
                return new XAResource[0];
            }
        };

        try {
            recovery.start();
            recovery.addHelper(helper);
            recovery.stop();
            assertTrue("XAResourceRecoveryHelper was not used", helperCalled.get());
        } catch (RecoveryException e) {
            fail(e.getMessage());
        }
    }
}
