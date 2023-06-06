import org.jboss.stm.annotations.Pessimistic;
import org.jboss.stm.annotations.Transactional;

@Transactional
@Pessimistic
// this is the default - so optional here
public interface Atomic {
    public int get();

    public void set(int value);
}