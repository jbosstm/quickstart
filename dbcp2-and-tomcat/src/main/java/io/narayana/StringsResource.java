/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2016, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package io.narayana;

import com.arjuna.ats.internal.jta.recovery.arjunacore.XARecoveryModule;
import com.arjuna.ats.jta.recovery.XAResourceRecoveryHelper;

import java.sql.SQLException;
import java.util.List;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.transaction.TransactionManager;
import javax.transaction.xa.XAResource;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import static java.lang.Thread.sleep;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
@Path("/")
public class StringsResource {

    private final StringDao stringDao;

    private final TransactionManager transactionManager;

    public StringsResource() throws NamingException, SQLException {
        stringDao = new StringDao();
        transactionManager = InitialContext.doLookup("java:comp/env/TransactionManager");
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> getStrings() throws SQLException {
        System.out.println(this.getClass().getSimpleName() + " GET");
        return stringDao.getAll();
    }

    @POST
    public void saveString(String string) throws Exception {
        System.out.println(this.getClass().getSimpleName() + " POST");
        System.out.println(this.getClass().getSimpleName() + " begin transaction");
        transactionManager.begin();
        System.out.println(this.getClass().getSimpleName() + " save string");
        try {
            stringDao.save(string);
            System.out.println(this.getClass().getSimpleName() + " commit transaction");
            transactionManager.commit();
            System.out.println(this.getClass().getSimpleName() + " transaction committed successfully");
        } catch (SQLException e) {
            System.out.println(this.getClass().getSimpleName() + " rollback transaction");
            transactionManager.rollback();
            System.out.println(this.getClass().getSimpleName() + " transaction rolled back");
            throw e;
        } finally {
            stringDao.close();
        }
    }

    @DELETE
    public void removeAll() throws Exception {
        System.out.println(this.getClass().getSimpleName() + " DELETE");
        stringDao.removeAll();
    }

    @POST
    @Path("crash")
    public void crash(String string) throws Exception {
        transactionManager.begin();
        try {
            transactionManager.getTransaction().enlistResource(new DummyXAResource(true));
            stringDao.save(string);
            transactionManager.commit();
        } catch (SQLException e) {
            transactionManager.rollback();
            throw e;
        } finally {
            stringDao.close();
        }
    }

    @GET
    @Path("recovery")
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> recovery() throws Exception{
        List<String> stringsBefore = stringDao.getAll();
        System.out.println("Strings at the start: " + stringsBefore);
        getXARecoveryModule().addXAResourceRecoveryHelper(new XAResourceRecoveryHelper() {
            @Override
            public boolean initialise(String s) throws Exception {
                return true;
            }

            @Override
            public XAResource[] getXAResources() throws Exception {
                return new DummyXAResourceRecovery().getXAResources();
            }
        });
        waitForRecovery(stringsBefore);
        System.out.println("Strings at the end: " + stringDao.getAll());
        return stringDao.getAll();
    }

    private void waitForRecovery(List<String> stringsBefore) throws Exception {
        boolean isComplete = false;

        for (int i = 0; i < 3 && !isComplete; i++) {
            sleep(5000);
            isComplete = stringsBefore.size() < stringDao.getAll().size();
        }

        if (isComplete) {
            System.out.println("Recovery completed successfully");
        } else {
            throw new Exception("Something wrong happened and recovery didn't complete");
        }
    }


    private XARecoveryModule getXARecoveryModule() {
        XARecoveryModule xaRecoveryModule = XARecoveryModule
                .getRegisteredXARecoveryModule();
        if (xaRecoveryModule != null) {
            return xaRecoveryModule;
        }
        throw new IllegalStateException(
                "XARecoveryModule is not registered with recovery manager");
    }
}
