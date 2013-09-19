package org.jboss.narayana.quickstarts.compensationsApi.taxi1;

/**
 * @author paul.robinson@redhat.com 18/09/2013
 */
public class BookingUnavailableRuntimeException extends RuntimeException {

    public BookingUnavailableRuntimeException(String s) {

        super(s);
    }

    public BookingUnavailableRuntimeException(String s, Throwable throwable) {

        super(s, throwable);
    }
}
