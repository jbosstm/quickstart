package com.arjuna.xts.nightout.services.recovery;

import jakarta.servlet.ServletContextListener;
import jakarta.servlet.ServletContextEvent;

/**
 * Listener to register and unregister teh XTS application specific listener -- we have to
 * use this because JBossWS does not currently honour the @PostConstruct and @PreDestroy
 * lifecycle annotations on web services
 */
public class DemoRPCATRecoveryListener implements ServletContextListener
{

    public void contextInitialized(ServletContextEvent event) {
        DemoRPCATRecoveryModule.register();
    }

    public void contextDestroyed(ServletContextEvent event) {
        DemoRPCATRecoveryModule.unregister();
    }
}