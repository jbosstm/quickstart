package io.narayana.recovery;

import com.arjuna.ats.arjuna.common.MetaObjectStoreEnvironmentBean;
import com.arjuna.ats.arjuna.common.RecoveryEnvironmentBean;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.arjuna.objectstore.ObjectStoreIterator;
import com.arjuna.ats.arjuna.objectstore.RecoveryStore;
import com.arjuna.ats.arjuna.objectstore.StoreManager;
import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import com.arjuna.ats.arjuna.recovery.RecoveryModule;
import com.arjuna.ats.internal.jta.recovery.arjunacore.XARecoveryModule;
import com.arjuna.ats.jta.recovery.XAResourceRecoveryHelper;
import com.arjuna.common.internal.util.propertyservice.BeanPopulator;
import io.narayana.config.Config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Recovery {
    public static String AtomicActionType = "/StateManager/BasicAction/TwoPhaseCoordinator/AtomicAction";

    private  RecoveryManager manager;
    private  XARecoveryModule xaRecoveryModule;

    public Recovery(Config config) {
        initialize(config);
    }

    public void setStoreDirectory(String storeDirectory) {
        MetaObjectStoreEnvironmentBean storeConfig =
                BeanPopulator.getDefaultInstance(MetaObjectStoreEnvironmentBean.class);
        storeConfig.setObjectStoreDir(storeDirectory);
    }

    public void initialize(Config config) {
        setStoreDirectory(config.storeDirectory);

        RecoveryEnvironmentBean recoveryEnvironmentBean = BeanPopulator.getDefaultInstance(RecoveryEnvironmentBean.class);
        recoveryEnvironmentBean.setRecoveryBackoffPeriod(config.recoveryBackoffPeriod);
        recoveryEnvironmentBean.setPeriodicRecoveryPeriod(config.periodicRecoveryPeriod);
        recoveryEnvironmentBean.setRecoveryModuleClassNames(config.recoveryModuleClassNames);
        recoveryEnvironmentBean.setExpiryScannerClassNames(null);
        recoveryEnvironmentBean.setRecoveryActivators(null);
    }

    public void start() throws RecoveryException {
        manager = RecoveryManager.manager();
        manager.initialize();

        // verify that the XARecoveryModule is configured
        for (RecoveryModule recoveryModule : (manager.getModules())) {
            if (recoveryModule instanceof XARecoveryModule) {
                this.xaRecoveryModule = (XARecoveryModule) recoveryModule;
                break;
            }
        }

        if (xaRecoveryModule == null) {
            throw new RecoveryException("This example requires the XARecoveryModule");
        }
    }

    public void stop() {
        manager.terminate();
    }


    public void addHelper(XAResourceRecoveryHelper helper) {
        xaRecoveryModule.addXAResourceRecoveryHelper(helper);
    }

    public void scan() {
        manager.scan();
    }

    public void suspend() {
        manager.suspend(false);
    }

    public void resume() {
        manager.resume();
    }

    public List<String> lookupActions(String type) throws RecoveryException {
        try {
            ObjectStoreIterator iter = new ObjectStoreIterator(StoreManager.getRecoveryStore(), type);
            List<String> uids = new ArrayList<>();
            do {
                Uid uid = iter.iterate();
                if (uid.equals(Uid.nullUid())) {
                    break;
                }
                uids.add(uid.fileStringForm());
            } while (true);

            return uids;
        } catch (ObjectStoreException | IOException e) {
            throw new RecoveryException(e);
        }
    }
}
