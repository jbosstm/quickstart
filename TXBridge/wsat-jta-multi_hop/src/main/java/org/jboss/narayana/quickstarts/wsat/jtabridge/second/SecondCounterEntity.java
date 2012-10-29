package org.jboss.narayana.quickstarts.wsat.jtabridge.second;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;

/**
 * @author paul.robinson@redhat.com, 2012-05-03
 */
@Entity
public class SecondCounterEntity implements Serializable {
    
    private int id;
    private int counter;

    public SecondCounterEntity() {
    }

    public SecondCounterEntity(int id, int initialCounterValue) {
        this.id = id;
        this.counter = initialCounterValue;
    }

    @Id
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCounter() {
        return counter;
    }

    public void setCounter(int counter) {
        this.counter = counter;
    }

    public void incrementCounter(int howMany) {
        setCounter(getCounter() + howMany);
    }
}