package io.narayana.recovery;

public class RecoveryException extends Exception {
    public RecoveryException(Exception e) {
        super(e);
    }

    public RecoveryException(String reason) {
        super(reason);
    }
}
