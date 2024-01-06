package es.jambo.outbox;
/**
 * @author Juan Manuel Sánchez Martagón <jmsanchezmartagon@gmail.com>
 */
public final class CouldNotCloseConnectionException extends RuntimeException {

    public CouldNotCloseConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
