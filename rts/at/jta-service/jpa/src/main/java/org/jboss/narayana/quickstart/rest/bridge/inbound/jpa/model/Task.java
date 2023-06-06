package org.jboss.narayana.quickstart.rest.bridge.inbound.jpa.model;

import static jakarta.persistence.GenerationType.IDENTITY;

import java.io.Serializable;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

import jakarta.json.Json;
import jakarta.json.JsonObject;

/**
 * User's task entity which is marked up with JPA annotations and JAXB for serializing XML
 * 
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 * @author Oliver Kiss
 */
@SuppressWarnings("serial")
@Entity
public class Task implements Serializable {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @ManyToOne
    private UserTable owner;

    private String title;

    public Task() {
    }

    public Task(String title) {
        super();
        this.title = title;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UserTable getOwner() {
        return owner;
    }

    public String getOwnerName() {
        return owner.getUsername();
    }

    public void setOwner(UserTable owner) {
        this.owner = owner;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((owner == null) ? 0 : owner.hashCode());
        result = prime * result + ((title == null) ? 0 : title.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        Task other = (Task) obj;

        if (owner == null && other.owner != null) {
            return false;
        } else if (!owner.equals(other.owner)) {
            return false;
        }

        if (title == null && other.title != null) {
            return false;
        } else if (!title.equals(other.title)) {
            return false;
        }

        return true;
    }

    /**
     * Returns JSON representation of task object.
     * 
     * @return
     */
    public JsonObject toJson() {
        return Json.createObjectBuilder()
                .add("id", id)
                .add("owner", owner.getUsername())
                .add("title", title)
                .build();
    }

}