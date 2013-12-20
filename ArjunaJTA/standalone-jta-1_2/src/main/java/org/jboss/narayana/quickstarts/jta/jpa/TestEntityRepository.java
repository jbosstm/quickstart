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


public class TestEntityRepository {

    @Inject
    EntityManager entityManager;

    @Transactional
    public List<TestEntity> findAll() {
        assert entityManager != null;
        return (List<TestEntity>) this.entityManager.createQuery("select te from TestEntity te").getResultList();
    }

    @Transactional
    public Long save(TestEntity testEntity) {
        assert entityManager != null;
        if (testEntity.isTransient()) {
            entityManager.persist(testEntity);
            entityManager.flush();
        } else {
            entityManager.merge(testEntity);
            entityManager.flush();
        }
        return testEntity.getId();
    }
}
