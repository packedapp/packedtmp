package app.packed.container;

import app.packed.application.ApplicationDriver;

/**
 * An exception indicating that a particular extension is not available for use.
 * 
 * <p>
 * This exception is typically in one of the following situations:
 * <ul>
 * <li><b>Use of a disabled Extension.</b> A particular extension was disabled, for example, as determined by
 * {@link ApplicationDriver#disabledExtensions()}. But an attempt was made to use it anyway.</li>
 * <li><b>An extension tried to use an undeclared dependency.</b> A extension tried to another extension that it had not
 * explicitly declared as a dependency</li>
 * </ul>
 * 
 * 
 * Indicate An exception that is typically thrown when attempting to perform an operation that requires an application
 * to be runnable, but application was not.
 * 
 * @see ApplicationDriver#hasRuntime()
 */


// Eller hvad er det maaske ikke federe at smide en InternalExtensionException

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
