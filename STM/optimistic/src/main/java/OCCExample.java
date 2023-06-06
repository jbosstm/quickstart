import org.jboss.stm.Container;
import org.jboss.stm.Container.MODEL;
import org.jboss.stm.Container.TYPE;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.coordinator.ActionStatus;

/**
 * This example shows that the locks for STM optimistic mode are evaluated during the completion operation
 */
public class OCCExample {

    public static void main(String[] args) throws Exception {

        Container<Atomic> container = new Container<Atomic>();

        final Atomic obj1 = container.create(new ExampleSTM());
        workaroundJBTM1732(obj1);
        final Atomic obj2 = container.clone(new ExampleSTM(), obj1);

        AtomicAction a = new AtomicAction();
        a.begin();
        obj1.set(1234);
        AtomicAction.suspend();

        AtomicAction b = new AtomicAction();
        b.begin();
        obj2.set(12345);
        b.commit();

        AtomicAction.resume(a);
        if (a.commit() != ActionStatus.ABORTED) {
            throw new RuntimeException("AtomicAction should have failed to commit");
        }

        AtomicAction c = new AtomicAction();
        c.begin();
        if (obj1.get() != 12345 || obj2.get() != 12345) {
            throw new RuntimeException("Object had unexpected state");
        }
        c.commit();
    }

    /**
     * Make sure there's state on "disk" before we try to do anything with a shared instance.
     * 
     * https://issues.jboss.org/browse/JBTM-1732
     */
    private static void workaroundJBTM1732(Atomic obj1) {
        AtomicAction act = new AtomicAction();
        act.begin();
        obj1.set(obj1.get());
        act.commit();
    }

}