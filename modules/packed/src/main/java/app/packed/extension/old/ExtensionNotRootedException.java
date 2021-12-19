package app.packed.extension.old;

import app.packed.build.BuildException;

/**
 * An exception that is thrown at build time when a {@link RootedExtension} is not rooted in an application.
 */
public class ExtensionNotRootedException extends BuildException {

    /** */
    private static final long serialVersionUID = 1L;

    public ExtensionNotRootedException(String message) {
        super(message);
    }

    public ExtensionNotRootedException(String message, Throwable cause) {
        super(message, cause);
    }
}
