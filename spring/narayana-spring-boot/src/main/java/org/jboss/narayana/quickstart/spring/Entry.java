package org.jboss.narayana.quickstart.spring;

import java.util.Objects;

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

    private String val;

    Entry() {
    }

    public Entry(String val) {
        this.val = val;
    }

    public String getVal() {
        return this.val;
    }

    @Override
    public String toString() {
        return "Entry{id=" + this.id + ", value='" + this.val + "'}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Entry entry = (Entry) o;
        return Objects.equals(this.id, entry.id) && Objects.equals(this.val, entry.val);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id, this.val);
    }
}