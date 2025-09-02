package io.narayana.config;

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class BasicTest {
    static Config config;
    static String nodeName = "1";

    @BeforeClass
    public static void beforeClass() {
        config = new Config(
                nodeName,
                "target",
                1,
                1,
                List.of(
                        "com.arjuna.ats.internal.arjuna.recovery.AtomicActionRecoveryModule",
                        "com.arjuna.ats.internal.jta.recovery.arjunacore.XARecoveryModule")
                );
    }

    @Test
    public void testConfig() {
        assertEquals(config.nodeName, nodeName);
        assertEquals(2, config.recoveryModuleClassNames.size());
    }
}
