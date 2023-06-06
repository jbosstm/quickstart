package org.jboss.narayana.quickstart.jta;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.inject.Disposes;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

/**
 * CDI producer of the EntityManagerFactory.
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
@Singleton
public class EntityManagerFactoryProducer {

    private EntityManagerFactory entityManagerFactory;

    @PostConstruct
    public void postConstruct() {
        entityManagerFactory = Persistence.createEntityManagerFactory("quickstart-persistence-unit");
    }

    @Produces
    public EntityManagerFactory createEntityManagerFactory() {
        return entityManagerFactory;
    }

    public void close(@Disposes EntityManagerFactory entityManagerFactory) {
        entityManagerFactory.close();
    }
}