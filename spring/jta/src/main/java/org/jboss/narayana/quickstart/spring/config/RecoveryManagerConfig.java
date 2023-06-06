package org.jboss.narayana.quickstart.spring.config;

import com.arjuna.ats.internal.jta.recovery.arjunacore.XARecoveryModule;
import com.arjuna.ats.jbossatx.jta.RecoveryManagerService;
import com.arjuna.ats.jta.recovery.XAResourceRecoveryHelper;
import org.jboss.narayana.quickstart.spring.xa.DummyXAResourceRecovery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.XADataSource;
import javax.transaction.xa.XAResource;
import java.sql.SQLException;

@Configuration
public class RecoveryManagerConfig {
    @Autowired
    private XADataSource ds;

    @Bean
    public RecoveryManagerService recoveryManagerService() {
        RecoveryManagerService rms = new RecoveryManagerService();
        rms.create();
        rms.addXAResourceRecovery(xaResourceRecovery());
        XARecoveryModule xaRecoveryModule = XARecoveryModule
                .getRegisteredXARecoveryModule();
        if (xaRecoveryModule != null) {
            xaRecoveryModule.addXAResourceRecoveryHelper( new XAResourceRecoveryHelper() {
                @Override
                public boolean initialise(String p) throws Exception {
                    return true;
                }

                @Override
                public XAResource[] getXAResources() throws Exception {
                    try {
                        return new XAResource[] { ds.getXAConnection().getXAResource() };
                    } catch (SQLException ex) {
                        return new XAResource[0];
                    }
                }
            });
        }

        return rms;
    }

    @Bean
    public DummyXAResourceRecovery xaResourceRecovery() {
        return new DummyXAResourceRecovery();
    }
}