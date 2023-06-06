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

        AtomicAction.resume(a);
        if (a.commit() != ActionStatus.COMMITTED) {
            throw new RuntimeException("AtomicAction should have failed to commit");
        }

        AtomicAction c = new AtomicAction();
        c.begin();
        if (obj1.get() != 1234 || obj2.get() != 1234) {
            throw new RuntimeException("Object had unexpected state");
        }
        c.commit();
    }
}