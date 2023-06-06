package org.jboss.narayana.quickstart.rest.bridge.inbound.jpa.model;

import java.util.List;

import jakarta.ejb.Local;

/**
 * Basic operations for manipulation of tasks
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 * @author Lukas Fryc
 *
 */
@Local
public interface TaskDao {

    void createTask(UserTable user, Task task);

    List<Task> getAll(UserTable user);

    List<Task> getRange(UserTable user, int offset, int count);

    List<Task> getForTitle(UserTable user, String title);

    void deleteTask(Task task);
    
    void deleteTasks();
}