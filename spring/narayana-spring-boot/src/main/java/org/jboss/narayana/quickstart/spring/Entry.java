package org.jboss.narayana.quickstart.spring;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
@Entity
public class Entry {

    @Id
    @GeneratedValue
    private Long id;

    private String value;

    Entry() {

    }

    public Entry(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "Entry{id=" + id + ", value='" + value + "'}";
    }
}