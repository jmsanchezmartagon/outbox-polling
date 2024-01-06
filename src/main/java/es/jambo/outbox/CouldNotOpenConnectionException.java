package es.jambo.outbox;
/**
 * @author Juan Manuel Sánchez Martagón <jmsanchezmartagon@gmail.com>
 */
public final class CouldNotOpenConnectionException extends RuntimeException {

    public CouldNotOpenConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
