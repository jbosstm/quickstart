package io.narayana.util;

import java.util.Arrays;
import org.jboss.logging.Logger;

/**
 * Running code utilities.
 */
public final class CodeUtils {
    private static final Logger log = Logger.getLogger(CodeUtils.class);

    /**
     * Swallowing exception by printing it to log error.
     */
    public static void swallowClose(AutoCloseable closable) {
        try {
            closable.close();
        } catch (Throwable t) {
            log.errorf(t, "Closing '%s' and failed", closable.toString());
        }
    }

    /**
     * Closing multiple closable and possible exception reported as {@link RuntimeException}
     * with multiple suppressed added.
     * @param closable
     */
    public static void closeMultiple(AutoCloseable... closables) {
        RuntimeException re = null;
        for(AutoCloseable closable: closables) {
            try {
                log.tracef("Closing: %s", closable);
                closable.close();
            } catch (Exception t) {
                if(re == null) re = new RuntimeException("Error on closing multiple closable statemens: " + Arrays.asList(closables));
                re.addSuppressed(t);
            }
        }
        if(re != null) throw re;
    }

    /**
     * Run runnable and in case of exception swallowing it to print it only.
     */
    public static void swallowException(RunnableWithException r) {
        try {
            r.run();
        } catch (Throwable t) {
            log.errorf("Error on running '%s'", r);
        }
    }

    /**
     * See {@link #swallowException(RunnableWithException)}
     */
    public static void swallowExceptionMultiple(RunnableWithException... multiRunnables) {
        for(RunnableWithException runnable: multiRunnables) {
            swallowException(runnable);
        }
    }
}