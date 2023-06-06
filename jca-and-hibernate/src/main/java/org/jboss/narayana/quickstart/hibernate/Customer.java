package org.jboss.narayana.quickstart.hibernate;

import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Id;


/**
 * Customer entity has two fields which are both unique. Therefore, once a user will try to add a second customer with
 * the same name, exception will be thrown and transaction rolled back.
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "name" }))
public class Customer {

    private Long id;

    private String name;

    public Customer() {
        // This constructor is used by Hibernate
    }

    public Customer(final String name) {
        // This constructor is used by the application

        this.name = name;
    }

    @Id
    @GeneratedValue(generator = "increment")
    @GenericGenerator(name = "increment", strategy = "increment")
    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return new StringBuilder("<Customer: id=").append(id).append(", name=").append(name).append(">").toString();
    }

}