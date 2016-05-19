package org.jboss.narayana.quickstarts.jta;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class TestRuntimeException extends RuntimeException {

    public TestRuntimeException(String message) {
        super(message);
    }

}
