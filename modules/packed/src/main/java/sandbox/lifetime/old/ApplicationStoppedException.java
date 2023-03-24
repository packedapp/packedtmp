package sandbox.lifetime.old;

/**
 * An application that is typically thrown when an operation required a running application in order to be performed.
 * But the application was not running. For example, because it timedout while starting
 */
// Ideen er at det er naar vi f.eks. har lazy start og @Get -> starter lazy starter en applikation
// Men den fejler
// InvalidApplicationStateException?
// RuntimeApplicationException
// LifetimeNotRunning, LifetimeNotAvailable?
// NotRunningException
public class ApplicationStoppedException extends RuntimeException {

    /** */
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new exception.
     *
     * @param message
     *            the message
     */
    public ApplicationStoppedException(String message) {
        super(message);
    }

    /**
     * Creates a new exception.
     *
     * @param message
     *            the message
     * @param cause
     *            the cause
     */
    public ApplicationStoppedException(String message, Throwable cause) {
        super(message, cause);
    }
}
