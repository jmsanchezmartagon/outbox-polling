package es.jambo.outbox;

public final class CouldNotOpenConnectionException extends RuntimeException {

    public CouldNotOpenConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
