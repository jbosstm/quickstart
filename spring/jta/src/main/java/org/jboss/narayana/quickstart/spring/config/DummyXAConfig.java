package org.jboss.narayana.quickstart.spring.config;

import org.jboss.narayana.quickstart.spring.xa.DummyXAResource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DummyXAConfig {
    @Bean
    public DummyXAResource xaResource() {
        return new DummyXAResource();
    }
}