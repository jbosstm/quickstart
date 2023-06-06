import org.jboss.stm.annotations.ReadLock;
import org.jboss.stm.annotations.WriteLock;

public class ExampleSTM implements Atomic {
    @ReadLock
    public int get() {
        return state;
    }

    @WriteLock
    public void set(int value)
    {
        state = value;
    }

    private int state;
}