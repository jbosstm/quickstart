package io.narayana;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Properties;

import jakarta.transaction.TransactionManager;
import jakarta.transaction.TransactionSynchronizationRegistry;

import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import com.arjuna.ats.jbossatx.jta.RecoveryManagerService;
import com.arjuna.ats.jta.common.jtaPropertyManager;

import io.agroal.api.AgroalDataSource;
import io.agroal.api.configuration.supplier.AgroalDataSourceConfigurationSupplier;
import io.agroal.api.configuration.supplier.AgroalPropertiesReader;
import io.agroal.api.security.NamePrincipal;
import io.agroal.api.security.SimplePassword;
import io.agroal.api.transaction.TransactionIntegration;
import io.agroal.narayana.NarayanaTransactionIntegration;
import io.narayana.util.AgroalH2Utils;

/**
 * <p>
 * This is showcase how Agroal (https://agroal.github.io) can be set-up
 * as standalone application and how to configure its integration with Narayana.
 * <p>
 * There are shown two approaches for configuration in this class.
 * Both are valid and it's up to your preference which one you will use.  
 */
public class AgroalDatasource {
    private RecoveryManager recoveryManager;
    AgroalDataSource ds1, ds2;

    public void process(Runnable middleAction) throws Exception {
        TransactionManager transactionManager = com.arjuna.ats.jta.TransactionManager.transactionManager();
        TransactionSynchronizationRegistry transactionSynchronizationRegistry
            = new com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionSynchronizationRegistryImple();

        // intialize recovery manager while defining that it won't be run automatically
        // but user has to invoke it manually in the code
        recoveryManager = RecoveryManager.manager(RecoveryManager.DIRECT_MANAGEMENT);
        recoveryManager.initialize();
        // recovery service provides binding which Agroal needs for hooking the XAResource
        // to recovery manager which is then capable to work with the unfinished jdbc XA transactions 
        RecoveryManagerService recoveryManagerService = new RecoveryManagerService();
        recoveryManagerService.create();

        // fluent API to create Agroal datasource configuration
        AgroalDataSourceConfigurationSupplier configurationSupplier = new AgroalDataSourceConfigurationSupplier()
            .connectionPoolConfiguration(cp -> cp
                .transactionIntegration(new NarayanaTransactionIntegration(transactionManager, transactionSynchronizationRegistry,
                        "java:/agroalds1", false, recoveryManagerService))
                    .connectionFactoryConfiguration(cf -> cf
                        .jdbcUrl(String.format(AgroalH2Utils.DB_CONNECTION, AgroalH2Utils.DB_1))
                        .principal(new NamePrincipal(AgroalH2Utils.DB_USER))
                        .credential(new SimplePassword(AgroalH2Utils.DB_PASSWORD))
                        .recoveryPrincipal(new NamePrincipal(AgroalH2Utils.DB_PASSWORD))
                        .recoveryCredential(new SimplePassword(AgroalH2Utils.DB_USER))
                        .connectionProviderClassName(AgroalH2Utils.DB_XA_DATASOURCE)
                        // setting autocommit to false is necessary because H2 XAConnection implementation is wrong
                        // when there is XA transaction in-doubt the jdbc driver should not permit to change the autocommit mode to true
                        // it can do so only when transaction is finished
                        // we need to force the default value for false, then Agroal forces not changing the value
                        // even when transaction is finished but it's what we need for functionality of recovery could be tested
                        // normally in your application you will probably be not touching this settings!
                        .autoCommit(false))
                        .maxSize(10)
            );
        ds1 = AgroalDataSource.from(configurationSupplier);

        // properties to create Agroal datasource configuration
        Properties db2Agroal = new Properties();
        db2Agroal.setProperty(AgroalPropertiesReader.JDBC_URL, String.format(AgroalH2Utils.DB_CONNECTION, AgroalH2Utils.DB_2));
        db2Agroal.setProperty(AgroalPropertiesReader.PRINCIPAL, AgroalH2Utils.DB_USER);
        db2Agroal.setProperty(AgroalPropertiesReader.CREDENTIAL, AgroalH2Utils.DB_PASSWORD);
        db2Agroal.setProperty(AgroalPropertiesReader.RECOVERY_PRINCIPAL, AgroalH2Utils.DB_USER);
        db2Agroal.setProperty(AgroalPropertiesReader.RECOVERY_CREDENTIAL, AgroalH2Utils.DB_PASSWORD);
        db2Agroal.setProperty(AgroalPropertiesReader.PROVIDER_CLASS_NAME, AgroalH2Utils.DB_XA_DATASOURCE);
        db2Agroal.setProperty(AgroalPropertiesReader.MAX_SIZE, "10");
        AgroalPropertiesReader agroalReaderProperties2 = new AgroalPropertiesReader().readProperties(db2Agroal);
        AgroalDataSourceConfigurationSupplier agroalDataSourceConf2 = agroalReaderProperties2.modify();
        TransactionIntegration txIntegration2 = new NarayanaTransactionIntegration(
                com.arjuna.ats.jta.TransactionManager.transactionManager(), jtaPropertyManager.getJTAEnvironmentBean().getTransactionSynchronizationRegistry(),
                "java:/agroalds2", false, recoveryManagerService);
        agroalDataSourceConf2.connectionPoolConfiguration().transactionIntegration(txIntegration2);
        ds2 = AgroalDataSource.from(agroalDataSourceConf2);


        // starting transaction
        transactionManager.begin();

        Connection conn1 = ds1.getConnection();
        PreparedStatement ps1 = conn1.prepareStatement(AgroalH2Utils.INSERT_STATEMENT);
        ps1.setInt(1, 1);
        ps1.setString(2, "Arjuna");

        Connection conn2 = ds2.getConnection();
        PreparedStatement ps2 = conn2.prepareStatement(AgroalH2Utils.INSERT_STATEMENT);
        ps2.setInt(1, 1);
        ps2.setString(2, "Narayana");

        try {
            ps1.executeUpdate();
            ps1.close();
            middleAction.run();
            ps2.executeUpdate();
            ps2.close();
            transactionManager.commit();
        } catch (Exception e) {
            transactionManager.rollback();
            throw e;
        }
    }

    RecoveryManager getRecoveryManager() {
        if(recoveryManager == null)
            throw new NullPointerException("recoveryManager");
        return recoveryManager; 
    }

    void closeConnectionsAfterRecovery() {
        try { 
            ds1.close();
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }
        try { 
            ds2.close();

        } catch (Exception ignored) {
            ignored.printStackTrace();

        }
    }
    void closeConnections() {
        try { 
            ds1.getConnection().close();
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }
        try { 
            ds2.getConnection().close();

        } catch (Exception ignored) {
            ignored.printStackTrace();

        }
    }
}