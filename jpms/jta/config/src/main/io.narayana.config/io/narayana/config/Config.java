package io.narayana.config;

import java.util.List;

public class Config {
    public String nodeName;
    public String storeDirectory;
    public int recoveryBackoffPeriod;
    public int periodicRecoveryPeriod;
    public List<String> recoveryModuleClassNames;

    public Config(String nodeName,
                          String storeDirectory,
                          int recoveryBackoffPeriod,
                          int periodicRecoveryPeriod,
                          List<String> recoveryModuleClassNames) {
        this.nodeName = nodeName;
        this.storeDirectory = storeDirectory;
        this.recoveryBackoffPeriod = recoveryBackoffPeriod;
        this.periodicRecoveryPeriod = periodicRecoveryPeriod;
        this.recoveryModuleClassNames = recoveryModuleClassNames;
    }
}
