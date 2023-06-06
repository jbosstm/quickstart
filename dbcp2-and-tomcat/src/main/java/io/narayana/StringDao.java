package io.narayana;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class StringDao {

    private static final String CREATE_TABLE_QUERY = "CREATE TABLE IF NOT EXISTS strings (string VARCHAR(500))";

    private static final String FIND_ALL_STRINGS_QUERY = "SELECT string FROM strings";

    private static final String DELETE_ALL_STRINGS_QUERY = "DELETE FROM strings";

    private static final String INSERT_STRING_QUERY = "INSERT INTO strings VALUES ('%s')";

    private Connection connection;

    public StringDao() throws SQLException {
        initDatabase();
    }

    /**
     * Get all strings from the database.
     * 
     * @return
     * @throws SQLException
     */
    public List<String> getAll() throws SQLException {
        List<String> strings = new LinkedList<>();
        getConnection();
        try (Statement statement = connection.createStatement()) {
            try (ResultSet resultSet = statement.executeQuery(FIND_ALL_STRINGS_QUERY)) {
                while (resultSet.next()) {
                    strings.add(resultSet.getString("string"));
                }
            }
        }
        close();

        return strings;
    }

    /**
     * Save string to the database.
     * @param string
     * @throws SQLException
     */
    public void save(String string) throws SQLException {
        getConnection();
        try (Statement statement = connection.createStatement()) {
            statement.execute(String.format(INSERT_STRING_QUERY, string));
        }
        close();
    }

    /**
     * Delete all strings in the database.
     * @throws SQLException
     */
    public void removeAll() throws SQLException {
        getConnection();
        try (Statement statement = connection.createStatement()) {
            statement.execute(DELETE_ALL_STRINGS_QUERY);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        close();

    }

    /**
     * Create strings table if it doesn't exist.
     */
    private void initDatabase() throws SQLException {
        getConnection();
        try (Statement statement = connection.createStatement()) {
            statement.execute(CREATE_TABLE_QUERY);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        close();
    }

    private void getConnection() {
        if (connection == null) {
            try {
                DataSource ds = InitialContext.doLookup("java:comp/env/transactionalDataSource");
                connection = ds.getConnection();
            } catch (NamingException e) {
                throw new RuntimeException(e);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void close() throws SQLException {
        if (connection != null) {
            connection.close();
            connection = null;
        }
    }
}