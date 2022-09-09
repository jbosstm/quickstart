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
package org.jboss.narayana.quickstart.osgi;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.jboss.narayana.quickstart.xa.DummyXAResource;
import org.jboss.narayana.quickstart.xa.DummyXAResourceRecovery;
import org.jboss.tm.XAResourceRecovery;
import org.osgi.framework.BundleContext;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import jakarta.transaction.Transaction;
import jakarta.transaction.TransactionManager;
import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author <a href="mailto:zfeng@redhat.com">Amos Feng</a>
 */

@Command(scope = "narayana-quickstart", name = "testRecovery", description="The JTA Recovery Example")
@Service
public class RecoveryExample implements Action {
    @Reference
    private BundleContext context;

    @Reference
    private TransactionManager tm;

    @Option(name = "-f", aliases = "--force", description = "force the example to crash", required = false, multiValued = false)
    boolean force = false;

    @Override
    public Object execute() throws Exception {
        if (force) {
            System.out.println("testRecovery generate something to recovery");
            if (tm == null) {
                System.out.println("Can not get transaction manager");
            } else {
                DummyXAResource xa = new DummyXAResource(DummyXAResource.faultType.HALT);
                Context ctx = new InitialContext();
                DataSource ds = (DataSource)ctx.lookup("osgi:service/test");
                Connection conn = null;
                Statement stmt = null;

                try {
                    tm.begin();
                    Transaction transaction = tm.getTransaction();
                    transaction.enlistResource(xa);

                    conn = ds.getConnection();
                    stmt = conn.createStatement();
                    stmt.execute("CREATE TABLE IF NOT EXISTS example ( id varchar(10), name varchar(32) )");
                    stmt.execute("insert into example values (1, 'recovery')");

                    tm.commit();
                } catch (SQLException e) {
                    System.out.println(e);
                    tm.rollback();
                } finally {
                    stmt.close();
                    conn.close();
                }
            }
        } else {
            File file = new File("DummyXAResource/");
            if (file.exists() && file.isDirectory() && file.listFiles().length > 0) {
                int currentCommits = DummyXAResource.getCommitRequests();
                System.out.println("current commitRequests of the DummyXAResource is " + currentCommits);

                System.out.println("query the database before recovery");
                queryH2Database("select * from example");

                // The recovery should not happen until we register the DummyXAResourceRecovery
                System.out.println("register the DummyXAResourceRecovery");
                context.registerService(XAResourceRecovery.class, new DummyXAResourceRecovery(), null);

                // It's waiting for the recovery manager to scan the log store and find the crash record.
                // There should be something like the following to show the recovery is happening
                // [Periodic Recovery] DummyXAResourceRecovery Added DummyXAResource: 1f02408c-8ce0-4188-ae63-250619baf8ed_
                // [Periodic Recovery] DummyXAResourceRecovery returning list of DummyXAResources of length: 1
                // DummyXAResource XA_COMMIT  [< formatId=131077, gtrid_length=29, bqual_length=36, tx_uid=0:ffff7f000001:ac37:57070db7:10, node_name=1, branch_uid=0:ffff7f000001:ac37:57070db7:11, subordinatenodename=null, eis_name=0 >] with fault NONE
                //
                // DummyXAResouce.getCommitRequests() should be currentCommits + 1
                // query the database after the recovery, the last entry should be "id = 1, name = recovery" and the count of the entires should be plused one.
                System.out.println("testRecovery waiting ...");
                while (DummyXAResource.getCommitRequests() == currentCommits) {
                    Thread.sleep(1000);
                }

                System.out.println("testRecovery done");
                System.out.println("commitRequests of the DummyXAResource after recovery is " + DummyXAResource.getCommitRequests());

                if (DummyXAResource.getCommitRequests() != currentCommits + 1) {
                    System.err.println("RECOVERY the DummyXAResource could be WRONG !");
                }

                System.out.println("reopen the database");
                reopenH2Database();

                System.out.println("query the database after recovery");
                queryH2Database("select * from example");

            } else {
                System.out.println("you should run this command with -f at first");
            }
        }
        return null;
    }

    private void reopenH2Database() throws Exception {
        Context ctx = new InitialContext();
        DataSource ds = (DataSource) ctx.lookup("osgi:service/test");
        Connection conn = null;
        Statement stmt = null;

        try {
            conn = ds.getConnection();
            stmt = conn.createStatement();
            stmt.execute("shutdown");
        } catch (SQLException e) {
            System.out.println(e);
        } finally {
            stmt.close();
            conn.close();
        }
    }

    private void queryH2Database(String sql) throws Exception {
        Context ctx = new InitialContext();
        DataSource ds = (DataSource) ctx.lookup("osgi:service/test");
        Connection conn = null;
        Statement stmt = null;

        try {
            conn = ds.getConnection();
            stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet rs = stmt.executeQuery(sql);
            int rowCount = rs.last() ? rs.getRow() : 0;
            System.out.println(rowCount + " entries in the example");

            rs.beforeFirst();
            while (rs.next()) {
                String id = rs.getString("id");
                String name = rs.getString("name");
                System.out.println("id = " + id + ", name = " + name);
            }
        } catch (SQLException e) {
            System.out.println(e);
        } finally {
            stmt.close();
            conn.close();
        }
    }
}
