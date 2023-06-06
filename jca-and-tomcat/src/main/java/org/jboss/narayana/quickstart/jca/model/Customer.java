package org.jboss.narayana.quickstart.jca.model;

/**
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 *
 */
public final class Customer {

    private final int id;

    private final String name;

    public Customer(final int id, final String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return new StringBuilder("<Customer: id=").append(id).append(", name=").append(name).append(">").toString();
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (object == null) {
            return false;
        }

        if (!(object instanceof Customer)) {
            return false;
        }

        final Customer customer = (Customer) object;

        return id == customer.getId()
                && name.equals(customer.getName());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;

        result = prime * result + id;
        result = prime * result + name.hashCode();

        return result;
    }

}