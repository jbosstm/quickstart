package org.jboss.narayana.quickstart.jca.listener;

import java.net.MalformedURLException;
import java.net.URL;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import org.jboss.jca.embedded.Embedded;
import org.jboss.jca.embedded.EmbeddedFactory;
import org.jboss.logging.Logger;

/**
 * Bootstraps embedded Iron Jacamar container and JBossTS recovery manager on servlet context initialization and shuts them down when servlet is destroyed.
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 *
 */
@WebListener
public final class ServletContextListenerImpl implements ServletContextListener {

    private static final Logger LOG = Logger.getLogger(ServletContextListenerImpl.class);

    private static final String JDBC_RAR_FILE_NAME = "jdbc-xa.rar";

    private static final String POSTGRES_DS_FILE_NAME = "postgres-xa-ds.xml";

    private static final Embedded EMBEDDED = EmbeddedFactory.create();

    @Override
    public void contextInitialized(final ServletContextEvent servletContextEvent) {
        if (LOG.isTraceEnabled()) {
            LOG.trace("ServletContextListenerImpl.contextInitialized()");
        }

        LOG.info("Starting embedded Iron Jacamar container...");
        setUpJNDI();
        startContainer();
    }

    @Override
    public void contextDestroyed(final ServletContextEvent servletContextEvent) {
        if (LOG.isTraceEnabled()) {
            LOG.trace("ServletContextListenerImpl.contextDestroyed()");
        }

        LOG.info("Stopping embedded Iron Jacamar container...");
        stopContainer();
    }

    /**
     * Sets up JNDI server properties required for Iron Jacamar.
     */
    private void setUpJNDI() {
        System.setProperty("java.naming.factory.initial", "org.jnp.interfaces.NamingContextFactory");
        System.setProperty("java.naming.factory.url.pkgs", "org.jboss.naming:org.jnp.interfaces");
    }

    /**
     * Starts embedded Iron Jacamar container, deploys JDBC resource adapter, and Postgres data source.
     */
    private void startContainer() {
        try {
            EMBEDDED.startup();
            EMBEDDED.deploy(getURL(JDBC_RAR_FILE_NAME));
            EMBEDDED.deploy(getURL(POSTGRES_DS_FILE_NAME));
        } catch (Throwable e) {
            LOG.fatal("Container failed to start.", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Undeploys Postgres data source, JDBC resource adapter, and shuts down Iron Jacamar container.
     */
    private void stopContainer() {
        try {
            EMBEDDED.undeploy(getURL(POSTGRES_DS_FILE_NAME));
            EMBEDDED.undeploy(getURL(JDBC_RAR_FILE_NAME));
            EMBEDDED.shutdown();
        } catch (Throwable e) {
            LOG.fatal("Container failed to shut down.", e);
        }
    }

    private static URL getURL(final String fileName) throws MalformedURLException {
        return ServletContextListenerImpl.class.getClassLoader().getResource(fileName);
    }

}