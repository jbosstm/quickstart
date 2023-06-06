package org.jboss.narayana.quickstart.jta;

import java.util.List;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class QuickstartEntityRepository {

    @Inject
    private EntityManager entityManager;

    public List<QuickstartEntity> findAll() {
        assert entityManager != null;
        List<QuickstartEntity> entities = entityManager
                .createQuery("select qe from QuickstartEntity qe", QuickstartEntity.class).getResultList();
        return entities;
    }

    @Transactional
    public Long save(QuickstartEntity quickstartEntity) {
        assert entityManager != null;
        if (quickstartEntity.isTransient()) {
            entityManager.persist(quickstartEntity);
        } else {
            entityManager.merge(quickstartEntity);
        }
        System.out.println("Saved entity: " + quickstartEntity);
        return quickstartEntity.getId();
    }

    public void clear() {
        assert entityManager != null;
        findAll().forEach(entityManager::remove);
    }

}