package io.narayana.txuser;

public class TxUserException extends Exception {
    public TxUserException(Exception e) {
        super(e);
    }

    public TxUserException(String reason) {
        super(reason);
    }
}
