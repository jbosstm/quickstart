package org.jboss.narayana.quickstart.rest.bridge.inbound.jpa.model;

import jakarta.ejb.Local;

/**
 * Basic operations for manipulation with users
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 * @author Lukas Fryc
 *
 */
@Local
public interface UserTableDao {

    public UserTable getForUsername(String username);

    public void createUser(UserTable user);
    
    public void deleteUsers();
}