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
 *
 * @author JBoss, by Red Hat.
 */
import org.jboss.stm.Container;
import org.jboss.stm.Container.MODEL;
import org.jboss.stm.Container.TYPE;
import org.jboss.stm.LockException;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.coordinator.ActionStatus;

/**
 * This example shows that the locks for STM pessimistic mode are evaluated during the *Lock operation
 */
public class PCCExample {

    public static void main(String[] args) throws Exception {

        Container<Atomic> container = new Container<Atomic>();

        final Atomic obj1 = container.create(new ExampleSTM());
        final Atomic obj2 = container.clone(new ExampleSTM(), obj1);

        AtomicAction a = new AtomicAction();
        a.begin();
        obj1.set(1234);
        AtomicAction.suspend();

        AtomicAction b = new AtomicAction();
        b.begin();
        try {
            obj2.set(12345);
            throw new RuntimeException("Expected pessimistic lock failure");
        } catch (LockException e) {
            // This is expected for this test
        } finally {
            b.abort();
        }
        b.commit();

        AtomicAction.resume(a);
        if (a.commit() != ActionStatus.COMMITTED) {
            throw new RuntimeException("AtomicAction should have failed to commit");
        }

        AtomicAction c = new AtomicAction();
        c.begin();
        if (obj1.get() != 1234 || obj1.get() != 1234) {
            throw new RuntimeException("Object had unexpected state");
        }
        c.commit();
    }
}
