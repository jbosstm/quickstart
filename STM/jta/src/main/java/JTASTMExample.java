/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat, Inc. and/or its affiliates,
 * and individual contributors as indicated by the @author tags.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */
import jakarta.transaction.Transaction;
import jakarta.transaction.TransactionManager;

import org.jboss.stm.Container;
import org.jboss.stm.Container.MODEL;
import org.jboss.stm.Container.TYPE;

import com.arjuna.ats.arjuna.common.arjPropertyManager;

/**
 * This example shows that the locks for STM optimistic mode are evaluated during the completion operation
 */
public class JTASTMExample {

    public static void main(String[] args) throws Exception {
        arjPropertyManager.getCoreEnvironmentBean().setNodeIdentifier("1");

        // The default Container type is TYPE.RECOVERABLE and meaning updates to STM objects within the scope
        // of a transaction have no life outside of JVM execution.
        // As we are also using XA in this quickstart, we ensure the STM container is configured as persistent
        // to prevent violation of the XA contract.
        Container<Atomic> container = new Container<Atomic>(TYPE.PERSISTENT, MODEL.EXCLUSIVE);

        final Atomic obj1 = container.create(new ExampleSTM());
        workaroundJBTM1732(obj1);
        final Atomic obj2 = container.clone(new ExampleSTM(), obj1);

        TransactionManager tm = com.arjuna.ats.jta.TransactionManager
            .transactionManager();

        // Get the write lock
        tm.begin();
        obj1.set(100);
        Transaction suspend2 = tm.suspend();

        SimpleXAResource xar = new SimpleXAResource();
        tm.begin();
        tm.getTransaction().enlistResource(xar);
        obj2.set(10);

        Transaction suspend = tm.suspend();

        tm.resume(suspend2);
        tm.commit();

        tm.resume(suspend);

        try {
            tm.commit();
            throw new RuntimeException("Expected the OCC to violate and rollback the transaction");
        } catch (jakarta.transaction.RollbackException e) {
            // Expected
        }

        if (!xar.isRolledBack()) {
            throw new RuntimeException("STM violation did not cause rollback");
        }

        tm.begin();
        if (obj2.get() == 10) {
            throw new RuntimeException("Object was able to be set");
        }
        tm.commit();
    }

    /**
     * Make sure there's state on "disk" before we try to do anything with a shared instance.
     * 
     * https://issues.jboss.org/browse/JBTM-1732
     */
    private static void workaroundJBTM1732(Atomic obj1) throws Exception {
        TransactionManager tm = com.arjuna.ats.jta.TransactionManager
            .transactionManager();
        tm.begin();
        obj1.set(obj1.get());
        tm.commit();
    }
}
