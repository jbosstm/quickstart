package org.jboss.narayana.quickstart.spring.config;

import com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionManagerImple;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.jta.JtaTransactionManager;

import jakarta.transaction.TransactionManager;

@Configuration
@EnableTransactionManagement
public class TransactionConfig {
    @Bean
    public PlatformTransactionManager jtaTransactionManager() {
        JtaTransactionManager tm = new JtaTransactionManager();
        tm.setTransactionManager(transactionManager());
        return tm;
    }

    @Bean
    public TransactionManager transactionManager() {
        return new TransactionManagerImple();
    }
}