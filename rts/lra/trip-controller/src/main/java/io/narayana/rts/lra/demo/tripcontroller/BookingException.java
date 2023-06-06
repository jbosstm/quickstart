package io.narayana.rts.lra.demo.tripcontroller;

public class BookingException extends RuntimeException {
    int reason;

    public BookingException(int reason, String message) {
        super(message);

        this.reason = reason;
    }

    public int getReason() {
        return reason;
    }
}