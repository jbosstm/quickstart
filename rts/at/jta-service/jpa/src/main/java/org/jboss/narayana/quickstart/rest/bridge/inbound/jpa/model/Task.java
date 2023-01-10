/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
