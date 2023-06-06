package org.jboss.narayana.quickstarts.cmr;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Named
@RequestScoped
@Transactional
public class BookProcessorCmr extends BookProcessor {

    @PersistenceContext(unitName = "jdbc-cmr-datasource")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
}