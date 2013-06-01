package quickstart;

import org.jboss.narayana.rest.integration.api.Participant;
import org.jboss.narayana.rest.integration.api.Prepared;
import org.jboss.narayana.rest.integration.api.Vote;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

public class Work implements Participant, Serializable {
    // package scope since these two fields are accessed by the service (TransactionAwareResource)
    transient static AtomicInteger commitCnt = new AtomicInteger(0);
    transient static AtomicInteger abortCnt = new AtomicInteger(0);

    private int id;

    public Work(int id) {
        this.id = id;
    }

    @Override
    public Vote prepare() {
        System.out.printf("Service: preparing: wId=%d\n", id);
        return new Prepared();
    }

    @Override
    public void commit() {
        System.out.printf("Service: committing: wId=%d\n", id);

        if (String.valueOf(id).equals(TransactionAwareResource.FAIL_COMMIT)) {
            System.out.println("Service: Halting VM during commit of work unit wId=" + id);
            Runtime.getRuntime().halt(1);
        }

        commitCnt.incrementAndGet();
    }

    @Override
    public void commitOnePhase() {
        commit();
    }

    @Override
    public void rollback() {
        System.out.printf("Service: aborting: wId=%d\n", id);
        abortCnt.incrementAndGet();
    }

    public int getId() {
        return id;
    }
}
