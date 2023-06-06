package org.jboss.narayana.quickstarts.jta;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import java.util.List;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
@ApplicationScoped
public class QuickstartEntityRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public List<QuickstartEntity> findAll() {
        final Query query = entityManager.createQuery("select qe from QuickstartEntity qe");

        return (List<QuickstartEntity>) query.getResultList();
    }

    @Transactional(Transactional.TxType.MANDATORY)
    public Long save(QuickstartEntity quickstartEntity) {
        if (quickstartEntity.isTransient()) {
            entityManager.persist(quickstartEntity);
        } else {
            entityManager.merge(quickstartEntity);
        }

        return quickstartEntity.getId();
    }
}