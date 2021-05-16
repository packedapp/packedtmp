package app.packed.container;

import app.packed.application.ApplicationDriver;

/**
 * An extension that indicates that a particular extension is not available for use.
 * <p>
 * 
 * 
 * Indicate
 * An exception that is typically thrown when attempting to perform an operation that requires an application to be
 * runnable, but application was not.
 * 
 * @see ApplicationDriver#hasRuntime()
 */
// UnavailableExtensionException

//if disabled, or if application extension used by a non-root container
public class ExtensionNotAvailableException extends RuntimeException {

    /** */
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new exception.
     * 
     * @param message
     *            the message
     */
    public ExtensionNotAvailableException(String message) {
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
    public ExtensionNotAvailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
