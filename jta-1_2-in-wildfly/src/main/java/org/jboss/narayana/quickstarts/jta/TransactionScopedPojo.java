package org.jboss.narayana.quickstarts.jta;

import jakarta.transaction.TransactionScoped;
import java.io.Serializable;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
@TransactionScoped
public class TransactionScopedPojo implements Serializable {

    private int value = 0;

    public synchronized int getValue() {
        return value;
    }

    public synchronized void setValue(int value) {
        this.value = value;
    }

}