/*
 * JBoss, Home of Professional Open Source
 * Copyright 2019, Red Hat, Inc. and/or its affiliates, and individual
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

package org.jboss.narayana.quickstarts.jta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * This counter serves for testing and information
 * purposes to track events happening during the application
 * run. It's uses for tests to check if an event occurred.
 */
@ApplicationScoped
public class EventsCounter {
    private final Collection<String> events = new CopyOnWriteArrayList<>();

    public void clear() {
        events.clear();
    }

    public void addEvent(String event) {
        events.add(event);
    }

    public Collection<String> getEvents() {
        System.out.println(events);
        return new ArrayList<>(events);
    }

    public boolean containsEvent(String eventToCheck) {
        return events.stream()
            .anyMatch(event -> event.matches(".*" + eventToCheck + ".*"));
    }

    @Override
    public String toString() {
        return this.hashCode() + "[events:" + events + "]";
    }
}
