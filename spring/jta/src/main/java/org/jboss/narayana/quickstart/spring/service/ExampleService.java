package org.jboss.narayana.quickstart.spring.service;

import org.jboss.narayana.quickstart.spring.xa.DummyXAResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transaction;
import jakarta.transaction.TransactionManager;

@Service
public class ExampleService {
    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private DummyXAResource xaResource;

    @Autowired
    private TransactionManager tm;

    @PostConstruct
    public void setupTable() {
        jdbc.execute("CREATE TABLE IF NOT EXISTS example ( id varchar(10), name varchar(32) )");
    }

    @Transactional
    public void testCommit() throws Exception {
        try {
            System.out.println("testCommit");
            Transaction transaction = tm.getTransaction();
            transaction.enlistResource(xaResource);
            jdbc.execute("insert into example values (1, 'test1')");
            System.out.println("testCommit OK");
        } catch (Exception e) {
            System.out.println("testCommit FAIL with " + e);
            throw e;
        }
    }

    @Transactional
    public void testRecovery() {
        try {
            System.out.println("testRecovery");
            Transaction transaction = tm.getTransaction();
            xaResource.setFault(DummyXAResource.faultType.HALT);
            transaction.enlistResource(xaResource);
            jdbc.execute("insert into example values (1, 'test1')");
        } catch (Exception e) {
            System.out.println("testRecovery FAIL with " + e);
        }
    }

    public int checkRecord() {
        Integer count = jdbc.queryForObject("select count(*) from example", Integer.class);
        System.out.println("check in the database (count = " + count + ")");
        return count;
    }

    public void shutdownDatabase() {
        jdbc.execute("SHUTDOWN");
    }
}