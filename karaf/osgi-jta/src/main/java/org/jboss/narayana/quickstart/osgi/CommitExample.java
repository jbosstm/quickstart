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
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.jboss.narayana.quickstart.xa.DummyXAResource;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import jakarta.transaction.Transaction;
import jakarta.transaction.TransactionManager;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author <a href="mailto:zfeng@redhat.com">Amos Feng</a>
 */

@Command(scope = "narayana-quickstart", name = "testCommit", description = "The JTA Commit Example")
@Service
public class CommitExample implements Action {
    @Reference
    private TransactionManager tm;

    @Override
    public Object execute() throws Exception {
        System.out.println("testCommit");

        if (tm == null) {
            System.out.println("Can not get transaction manager");
        } else {
            DummyXAResource xa = new DummyXAResource();
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
                stmt.execute("insert into example values (1, 'commit')");

                tm.commit();
            } catch (SQLException e) {
                System.out.println(e);
                tm.rollback();
            } finally {
                stmt.close();
                conn.close();
            }
        }
        return null;
    }
}
