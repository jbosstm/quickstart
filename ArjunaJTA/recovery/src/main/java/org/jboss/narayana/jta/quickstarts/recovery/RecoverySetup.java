package org.jboss.narayana.jta.quickstarts.recovery;


import org.jboss.narayana.jta.quickstarts.util.Util;

import com.arjuna.ats.arjuna.common.ObjectStoreEnvironmentBean;
import com.arjuna.ats.arjuna.common.RecoveryEnvironmentBean;
import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import com.arjuna.common.internal.util.propertyservice.BeanPopulator;

public class RecoverySetup {
    protected static RecoveryManager recoveryManager;

    public static void startRecovery() {
        BeanPopulator.getDefaultInstance(ObjectStoreEnvironmentBean.class).setObjectStoreDir(Util.recoveryStoreDir);
        RecoveryManager.delayRecoveryManagerThread() ;
        BeanPopulator.getDefaultInstance(RecoveryEnvironmentBean.class).setRecoveryBackoffPeriod(1);
        recoveryManager = RecoveryManager.manager();
    }

    public static void stopRecovery() {
        recoveryManager.terminate();
    }

    protected void runRecoveryScan() {
        recoveryManager.scan();
    }
}