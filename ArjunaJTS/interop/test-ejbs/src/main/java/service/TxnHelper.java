/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2017, Red Hat, Inc., and individual contributors
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
package service;

import org.jboss.narayana.ASFailureMode;
import org.jboss.narayana.ASFailureSpec;
import org.jboss.narayana.ASFailureType;
import org.jboss.narayana.DummyXAResource;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.transaction.RollbackException;
import jakarta.transaction.SystemException;
import jakarta.transaction.Transaction;
import jakarta.transaction.TransactionManager;

public class TxnHelper {
    static private final String WL_TM = "weblogic.transaction.TransactionManager";
    static private final String EE_TM = "java:/TransactionManager";
    static private final String GF_TM = "java:appserver/TransactionManager";

    static void addResources(boolean isWF) throws NamingException, SystemException, RollbackException {
        addResources(isWF, null);
    }

    static void addResources(boolean isWF, String failureType) throws NamingException, SystemException, RollbackException {
        TransactionManager tm = getTransactionManager(isWF);

        assert tm.getTransaction() != null;

        tm.getTransaction().enlistResource(getResource(tm, failureType));
    }

    static TransactionManager getTransactionManager(boolean isWF) throws NamingException {
        if (isWF)
            return (TransactionManager) new InitialContext().lookup(EE_TM);
        else
            return (TransactionManager) new InitialContext().lookup(GF_TM);
    }

    static Transaction getTransaction(boolean isWF) throws NamingException, SystemException {
        return getTransactionManager(isWF).getTransaction();
    }

    static private DummyXAResource getResource(TransactionManager tm, String failureType) {
        if (failureType == null || tm == null)
            return new DummyXAResource();

        System.out.printf("enlisting dummy resource with fault type %s%n", failureType);

        if (failureType.contains("halt"))
            return new DummyXAResource(new ASFailureSpec("fault", ASFailureMode.HALT, "", ASFailureType.XARES_COMMIT));
        else
            return new DummyXAResource(new ASFailureSpec("", ASFailureMode.NONE, "", ASFailureType.NONE));
    }
}
