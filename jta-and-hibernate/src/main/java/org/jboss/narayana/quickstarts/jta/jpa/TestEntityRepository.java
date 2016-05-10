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
package org.jboss.narayana.quickstarts.jta.jpa;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.List;

@Transactional
public class TestEntityRepository {

    @Inject
    EntityManager entityManager;

    public List<TestEntity> findAll() {
        assert entityManager != null;
        List<TestEntity> entities = entityManager.createQuery("select te from TestEntity te", TestEntity.class).getResultList();
        System.out.println("Found entities: " + entities);
        return entities;
    }

    public Long save(TestEntity testEntity) {
        assert entityManager != null;
        if (testEntity.isTransient()) {
            entityManager.persist(testEntity);
        } else {
            entityManager.merge(testEntity);
        }
        System.out.println("Saved entity: " + testEntity);
        return testEntity.getId();
    }

    public void clear() {
        assert entityManager != null;
        findAll().forEach(entityManager::remove);
    }

}
