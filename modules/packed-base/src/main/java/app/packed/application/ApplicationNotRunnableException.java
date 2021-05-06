package app.packed.application;

/**
 * An exception that is typically thrown when attempting to perform an operation that requires an application to be
 * runnable, but application was not.
 * 
 * @see ApplicationDriver#hasRuntime()
 */
// NoRuntimeEnvironmentException
public class ApplicationNotRunnableException extends RuntimeException {

    /** */
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new exception.
     * 
     * @param message
     *            the message
     */
    public ApplicationNotRunnableException(String message) {
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
    public ApplicationNotRunnableException(String message, Throwable cause) {
        super(message, cause);
    }
}
