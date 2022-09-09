package io.narayana;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity(name = "strings")
@Table(name = "strings")
public class StringEntity {
    @Id
    @Column(name = "string", unique = false, length = 500)
    private String value;

    public StringEntity(String value) {
        setValue(value);
    }

    public StringEntity() {
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
