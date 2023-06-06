package org.jboss.narayana.quickstarts.jta;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.transaction.TransactionScoped;

/**
 * <p>
 * Transactional scoped counter.<br/>
 * Data of the counter are stored along the existence
 * of the particular transaction.
 * </p>
 * <p>
 * With starting a new transaction the injected counter
 * is initiated as a new instance.
 * When transaction finishes the counter is cleared up.
 * </p>
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
@TransactionScoped
public class Counter implements Serializable {
    private static final long serialVersionUID = 1L;

    private final AtomicInteger counter = new AtomicInteger();

    public int get() {
        return counter.get();
    }

    public void increment() {
        counter.incrementAndGet();
    }

    @Override
    public String toString() {
        return this.hashCode() + "[value:" + counter.get() + "]";
    }
}