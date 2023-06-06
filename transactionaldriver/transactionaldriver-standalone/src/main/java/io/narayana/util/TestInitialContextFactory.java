package io.narayana.util;

import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;

/**
 * Initial context returning stub of {@link TestContext} for quickstart.
 */
public class TestInitialContextFactory implements InitialContextFactory {
    private static final Context testContext = new TestContext();

    @Override
    public Context getInitialContext(Hashtable<?, ?> environment) throws NamingException {
        return testContext;
    }

}