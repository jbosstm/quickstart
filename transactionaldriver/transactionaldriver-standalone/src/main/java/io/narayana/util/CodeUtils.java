/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2017, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

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
