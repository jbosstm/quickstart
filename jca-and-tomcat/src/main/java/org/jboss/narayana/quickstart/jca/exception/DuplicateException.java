package org.jboss.narayana.quickstart.jca.exception;

/**
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 *
 */
public class DuplicateException extends Exception {

    private static final long serialVersionUID = -1227387070460841897L;

    public DuplicateException() {
        super();
    }

    public DuplicateException(String messagge) {
        super(messagge);
    }

}