package org.jboss.narayana.quickstarts.jta.jpa;

import com.arjuna.ats.jdbc.TransactionalDriver;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.hibernate.service.UnknownUnwrapTypeException;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class TransactionalConnectionProvider implements ConnectionProvider {

    private static final String DATABASE_JNDI = "java:/quickstartDataSource";

    private static final String USERNAME = "sa";

    private static final String PASSWORD = "";

    private final TransactionalDriver transactionalDriver;

    public TransactionalConnectionProvider() {
        transactionalDriver = new TransactionalDriver();
    }

    @Override
    public Connection getConnection() throws SQLException {
        System.out.println("TransactionalConnectionProvider.getConnection");
        Properties properties = new Properties();
        properties.setProperty(TransactionalDriver.userName, USERNAME);
        properties.setProperty(TransactionalDriver.password, PASSWORD);

        return transactionalDriver.connect("jdbc:arjuna:" + DATABASE_JNDI, properties);
//        return ConnectionManager.create(DATABASE_JNDI, properties);
    }

    @Override
    public void closeConnection(Connection connection) throws SQLException {
        System.out.println("TransactionalConnectionProvider.closeConnection");
        connection.close();
    }

    @Override
    public boolean supportsAggressiveRelease() {
        return false;
    }

    @Override
    public boolean isUnwrappableAs(Class aClass) {
        return getClass().isAssignableFrom(aClass);
    }

    @Override
    public <T> T unwrap(Class<T> aClass) {
        if (isUnwrappableAs(aClass)) {
            return (T) this;
        }

        throw new UnknownUnwrapTypeException(aClass);
    }

}
