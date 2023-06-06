package org.jboss.narayana.quickstart.rest.bridge.inbound.jpa.model;

import java.util.List;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

/**
 * Provides functionality for manipulation with users using persistence context.
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 * @author Lukas Fryc
 * @author Oliver Kiss
 *
 */
@Stateless
public class UserTableDaoImpl implements UserTableDao {

    @PersistenceContext
    EntityManager em;

    public UserTable getForUsername(String username) {
        List<UserTable> result = em.createQuery("select u from UserTable u where u.username = ?1", UserTable.class).setParameter(1, username)
                .getResultList();

        if (result.isEmpty()) {
            return null;
        }
        return result.get(0);
    }

    public void createUser(UserTable user) {
        em.persist(user);
    }
    
    public void deleteUsers() {
        em.createQuery("DELETE FROM UserTable").executeUpdate();
    }
}