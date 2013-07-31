package quickstart;

import org.jboss.narayana.rest.integration.api.Participant;
import org.jboss.narayana.rest.integration.api.Prepared;
import org.jboss.narayana.rest.integration.api.Vote;

import java.util.concurrent.atomic.AtomicInteger;

public class Work implements Participant {
    transient static AtomicInteger commitCnt = new AtomicInteger(0);

    private int id;

    public Work(int id) {
        this.id = id;
    }

    @Override
    public Vote prepare() {
        System.out.printf("Service: preparing: wId=%d%n", id);
        return new Prepared();
    }

    @Override
    public void commit() {
        System.out.printf("Service: committing: wId=%d%n", id);
        commitCnt.incrementAndGet();
    }

    @Override
    public void commitOnePhase() {
        commit();
    }

    @Override
    public void rollback() {
        System.out.printf("Service: aborting: wId=%d%n", id);
    }

    public int getId() {
        return id;
    }
}
