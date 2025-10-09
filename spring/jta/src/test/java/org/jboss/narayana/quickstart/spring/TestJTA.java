package org.jboss.narayana.quickstart.spring;

import static org.junit.Assert.assertEquals;

import org.jboss.narayana.quickstart.spring.config.DatabaseConfig;
import org.jboss.narayana.quickstart.spring.config.DummyXAConfig;
import org.jboss.narayana.quickstart.spring.xa.DummyXAResource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import dev.snowdrop.boot.narayana.autoconfigure.NarayanaAutoConfiguration;
import jakarta.annotation.Resource;
import jakarta.transaction.TransactionManager;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = { NarayanaAutoConfiguration.class,
        DatabaseConfig.class, DummyXAConfig.class})
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