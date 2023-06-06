package io.narayana;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
@ApplicationPath("/")
public class StringsApplication extends Application {

    private final EntityManagerFactory emf;

    public StringsApplication() {
        emf = Persistence.createEntityManagerFactory("quickstart-persistence-unit");
        StringDao.entityManager = emf.createEntityManager();
    }
}