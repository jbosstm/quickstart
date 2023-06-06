import org.jboss.stm.annotations.Optimistic;
import org.jboss.stm.annotations.Transactional;

@Transactional
@Optimistic
public interface Atomic {
    public int get();

    public void set(int value);
}