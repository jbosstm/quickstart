package io.narayana;


import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class StringDao {

    public static EntityManager entityManager;

    /**
     * Get all strings from the database. This methods closes database connection itself, thus transaction is not needed.
     *
     * @return
     * @throws SQLException
     */
    public List<String> getAll() throws SQLException {
        List<StringEntity> entities = entityManager
                .createQuery("SELECT string FROM strings string", StringEntity.class).setLockMode(LockModeType.PESSIMISTIC_READ).getResultList();
        List<String> strings = new ArrayList<String>();

        for (StringEntity entity : entities) {
            strings.add(entity.getValue());
        }
        return strings;
    }

    /**
     * Save string to the database. This method must be called inside a transaction, because connection is left open to be
     * closed by transaction manager.
     *
     * @param string
     * @throws SQLException
     */
    public void save(String string) throws SQLException {
        entityManager.persist(new StringEntity(string));
    }

}