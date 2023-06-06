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