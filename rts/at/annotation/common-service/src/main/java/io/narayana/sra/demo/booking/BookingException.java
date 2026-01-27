package io.narayana.sra.demo.booking;

public class BookingException extends Exception {
    int reason;

    public BookingException(int reason, String message) {
        super(message);

        this.reason = reason;

    }

    public int getReason() {
        return reason;
    }
}
