package org.jboss.narayana.quickstart.rest.bridge.inbound.jpa.model;

import java.util.List;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

/**
 * Provides functionality for manipulation with tasks using the persistence context.
 * 
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 * @author Lukas Fryc
 * @author Oliver Kiss
 *
 */
@Stateless
public class TaskDaoImpl implements TaskDao {

    @PersistenceContext
    EntityManager em;

    @Override
    public void createTask(UserTable user, Task task) {
        if (!em.contains(user)) {
            user = em.merge(user);
        }
        user.getTasks().add(task);
        task.setOwner(user);
        em.persist(task);
    }

    @Override
    public List<Task> getAll(UserTable user) {
        TypedQuery<Task> query = querySelectAllTasksFromUser(user);
        return query.getResultList();
    }

    @Override
    public List<Task> getRange(UserTable user, int offset, int count) {
        TypedQuery<Task> query = querySelectAllTasksFromUser(user);
        query.setMaxResults(count);
        query.setFirstResult(offset);
        return query.getResultList();
    }

    @Override
    public List<Task> getForTitle(UserTable user, String title) {
        String lowerCaseTitle = "%" + title.toLowerCase() + "%";
        return em.createQuery("SELECT t FROM Task t WHERE t.owner = ?1 AND LOWER(t.title) LIKE ?2", Task.class)
                .setParameter(1, user).setParameter(2, lowerCaseTitle).getResultList();
    }

    @Override
    public void deleteTask(Task task) {
        if (!em.contains(task)) {
            task = em.merge(task);
        }
        em.remove(task);
    }
    
    @Override
    public void deleteTasks() {
        em.createQuery("DELETE FROM Task").executeUpdate();
    }

    private TypedQuery<Task> querySelectAllTasksFromUser(UserTable user) {
        return em.createQuery("SELECT t FROM Task t WHERE t.owner = ?1", Task.class).setParameter(1, user);
    }
}