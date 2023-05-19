package es.jambo.outbox;

public final class CouldNotCloseConnectionException extends RuntimeException {

    public CouldNotCloseConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
