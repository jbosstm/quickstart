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
