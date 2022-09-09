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
package org.jboss.narayana.quickstart.spring;

import org.jboss.narayana.quickstart.spring.config.DatabaseConfig;
import org.jboss.narayana.quickstart.spring.config.DummyXAConfig;
import org.jboss.narayana.quickstart.spring.config.TransactionConfig;
import org.jboss.narayana.quickstart.spring.xa.DummyXAResource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import jakarta.annotation.Resource;
import jakarta.transaction.TransactionManager;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class,
        classes = {TransactionConfig.class, DatabaseConfig.class, DummyXAConfig.class})
public class TestJTA {
    @Resource
    private TransactionManager tm;

    @Resource
    private JdbcTemplate jdbc;

    @Resource
    private DummyXAResource xaResource;

    @Before
    public void setUp() {
        jdbc.execute("create table test ( id varchar(10), name varchar(32) )");
    }

    @After
    public void tearDown() {
        jdbc.execute("drop table test");
        jdbc.execute("SHUTDOWN");
    }

    @Test
    public void testCommit() throws Exception {
        tm.begin();
        tm.getTransaction().enlistResource(xaResource);
        jdbc.execute("insert into test values (1, 'test1')");
        tm.commit();

        checkCount(1);
    }

    private void checkCount(int count) {
        assertEquals(count, jdbc.queryForObject("select count(*) from test", Integer.class).intValue());
    }

}
