/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.narayana.quickstart.jta;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;

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
